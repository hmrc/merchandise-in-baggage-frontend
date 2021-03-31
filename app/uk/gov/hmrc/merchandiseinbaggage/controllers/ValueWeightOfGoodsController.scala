/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.goodsDestinationUnansweredMessage
import uk.gov.hmrc.merchandiseinbaggage.forms.ValueWeightOfGoodsForm.form
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.ValueWeightOfGoodsView

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.merchandiseinbaggage.navigation._

@Singleton
class ValueWeightOfGoodsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  navigator: Navigator,
  view: ValueWeightOfGoodsView)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    backToCheckYourAnswersIfCompleteElse(routes.ExciseAndRestrictedGoodsController.onPageLoad())

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    request.declarationJourney.maybeGoodsDestination
      .fold(actionProvider.invalidRequest(goodsDestinationUnansweredMessage)) { goodsDestination =>
        Ok(
          view(
            request.declarationJourney.maybeValueWeightOfGoodsBelowThreshold.fold(form(goodsDestination))(form(goodsDestination).fill),
            goodsDestination,
            request.declarationType,
            backButtonUrl
          ))
      }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    val declarationJourney = request.declarationJourney

    declarationJourney.maybeGoodsDestination
      .fold(actionProvider.invalidRequestF(goodsDestinationUnansweredMessage)) { goodsDestination =>
        form(goodsDestination)
          .bindFromRequest()
          .fold(
            formWithErrors => Future successful BadRequest(view(formWithErrors, goodsDestination, request.declarationType, backButtonUrl)),
            belowThreshold => {
              persistAndRedirect(
                declarationJourney.copy(maybeValueWeightOfGoodsBelowThreshold = Some(belowThreshold)),
                navigator.nextPage(RequestByPassWithIndexAndValue(belowThreshold, declarationJourney.goodsEntries.entries.size))
              )
            }
          )
      }
  }
}

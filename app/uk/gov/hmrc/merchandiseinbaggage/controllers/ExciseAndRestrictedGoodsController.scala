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
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.ExciseAndRestrictedGoodsForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.core.GoodsDestinations.NorthernIreland
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo.Yes
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.ExciseAndRestrictedGoodsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExciseAndRestrictedGoodsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: ExciseAndRestrictedGoodsView)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]): Call =
    backToCheckYourAnswersIfCompleteElse(
      if (request.declarationJourney.maybeGoodsDestination.contains(NorthernIreland))
        routes.GoodsRouteDestinationController.onPageLoad()
      else routes.GoodsDestinationController.onPageLoad()
    )

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(
      view(
        request.declarationJourney.maybeExciseOrRestrictedGoods
          .fold(form(request.declarationType))(form(request.declarationType).fill),
        request.declarationJourney.declarationType,
        backButtonUrl
      ))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form(request.declarationType)
      .bindFromRequest()
      .fold(
        formWithErrors => Future successful BadRequest(view(formWithErrors, request.declarationJourney.declarationType, backButtonUrl)),
        value => {
          persistAndRedirect(
            request.declarationJourney.copy(maybeExciseOrRestrictedGoods = Some(value)),
            if (value == Yes) routes.CannotUseServiceController.onPageLoad()
            else routes.ValueWeightOfGoodsController.onPageLoad()
          )
        }
      )
  }
}

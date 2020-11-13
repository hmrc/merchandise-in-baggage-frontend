/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.ValueWeightOfGoodsForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo._
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ValueWeightOfGoodsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ValueWeightOfGoodsController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                             actionProvider: DeclarationJourneyActionProvider,
                                             override val repo: DeclarationJourneyRepository,
                                             view: ValueWeightOfGoodsView)
                                            (implicit ec: ExecutionContext, appConfig: AppConfig)
  extends DeclarationJourneyUpdateController {

  private val backButtonUrl: Call = routes.ExciseAndRestrictedGoodsController.onPageLoad()

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    request.declarationJourney.maybeGoodsDestination match {
      case Some(dest) => Ok(
        view(request.declarationJourney.maybeValueWeightOfGoodsExceedsThreshold.fold(form)(form.fill), dest, backButtonUrl)
      )
      case None => Redirect(routes.GoodsDestinationController.onPageLoad())
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.maybeGoodsDestination match {
      case Some(dest) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future successful BadRequest(view(formWithErrors, dest, backButtonUrl)),
            exceedsThreshold => {
              persistAndRedirect(
                request.declarationJourney.copy(maybeValueWeightOfGoodsExceedsThreshold = Some(exceedsThreshold)),
                if (exceedsThreshold == Yes) routes.CannotUseServiceController.onPageLoad()
                else routes.GoodsTypeQuantityController.onPageLoad(1)
              )
            }
          )
      case None =>
        Future successful Redirect(routes.GoodsDestinationController.onPageLoad())
    }
  }
}

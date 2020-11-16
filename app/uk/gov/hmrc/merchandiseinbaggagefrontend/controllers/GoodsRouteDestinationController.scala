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

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import play.api.routing.Router.empty.routes
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.GoodsRouteDestinationForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationType._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestinations.NorthernIreland
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.GoodsRouteDestinationView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GoodsRouteDestinationController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                                actionProvider: DeclarationJourneyActionProvider,
                                                override val repo: DeclarationJourneyRepository,
                                                view: GoodsRouteDestinationView,
                                               )(implicit appConf: AppConfig)
  extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]): Call =
    if (request.declarationJourney.maybeGoodsDestination.contains(NorthernIreland))
      routes.GoodsDestinationController.onPageLoad()
    else routes.GoodsDestinationController.onPageLoad()

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(view(
      request.declarationJourney.maybeGoodsRouteDestination.fold(form)(form.fill),
      request.declarationJourney.declarationType,
      backButtonUrl))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, request.declarationJourney.declarationType, backButtonUrl))),
        value => {
          persistAndRedirect(
            request.declarationJourney.copy(maybeGoodsRouteDestination = Some(value)),
            if (value == Yes) routes.InvalidRequestController.onPageLoad()
            else routes.ExciseAndRestrictedGoodsController.onPageLoad()
          )
        }
      )
  }
}

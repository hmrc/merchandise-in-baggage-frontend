/*
 * Copyright 2023 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.forms.VehicleSizeForm.form
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.VehicleSizeView
import uk.gov.hmrc.merchandiseinbaggage.navigation._

import scala.concurrent.{ExecutionContext, Future}

class VehicleSizeController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  navigator: Navigator,
  view: VehicleSizeView
)(implicit ec: ExecutionContext, appConf: AppConfig)
    extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    backToCheckYourAnswersIfCompleteElse(GoodsInVehicleController.onPageLoad)

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(
      view(
        request.declarationJourney.maybeTravellingBySmallVehicle
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
        isSmallVehicle => {
          val updated = request.declarationJourney.copy(maybeTravellingBySmallVehicle = Some(isSmallVehicle))
          navigator
            .nextPage(
              VehicleSizeRequest(
                isSmallVehicle,
                updated,
                repo.upsert,
                updated.declarationRequiredAndComplete
              ))
            .map(Redirect)
        }
      )
  }
}

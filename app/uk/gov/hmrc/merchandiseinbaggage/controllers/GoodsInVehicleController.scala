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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.GoodsInVehicleForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo.Yes
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsInVehicleView

import scala.concurrent.{ExecutionContext, Future}

class GoodsInVehicleController @Inject()(
                                          override val controllerComponents: MessagesControllerComponents,
                                          actionProvider: DeclarationJourneyActionProvider,
                                          override val repo: DeclarationJourneyRepository,
                                          view: GoodsInVehicleView,
                                        )(implicit ec: ExecutionContext, appConf: AppConfig) extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    backToCheckYourAnswersIfCompleteElse(routes.JourneyDetailsController.onPageLoad())

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(view(request.declarationJourney.maybeTravellingByVehicle.fold(form)(form.fill), request.declarationType, backButtonUrl))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future successful BadRequest(view(formWithErrors, request.declarationType, backButtonUrl)),
        goodsInVehicle =>
          persistAndRedirect(
            request.declarationJourney.copy(maybeTravellingByVehicle = Some(goodsInVehicle)),
            if (goodsInVehicle == Yes) routes.VehicleSizeController.onPageLoad()
            else routes.CheckYourAnswersController.onPageLoad()
          )
      )
  }
}

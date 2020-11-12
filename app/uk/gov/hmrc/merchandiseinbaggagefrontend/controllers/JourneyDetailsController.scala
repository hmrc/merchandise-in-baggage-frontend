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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.JourneyDetailsForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.JourneyDetailsPage

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyDetailsController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                         actionProvider: DeclarationJourneyActionProvider,
                                         repo: DeclarationJourneyRepository,
                                         view: JourneyDetailsPage)
                                        (implicit ec: ExecutionContext, appConfig: AppConfig)
  extends DeclarationJourneyUpdateController {

  private val backButtonUrl: Call = routes.EnterEmailController.onPageLoad()

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(view(request.declarationJourney.maybeJourneyDetailsEntry.fold(form)(details => form.fill(details)), backButtonUrl))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, backButtonUrl))),
        journeyDetailsEntry =>
          repo.upsert(request.declarationJourney.copy(maybeJourneyDetailsEntry = Some(journeyDetailsEntry))).map { _ =>
            if (journeyDetailsEntry.placeOfArrival.vehiclePort) Redirect(routes.GoodsInVehicleController.onPageLoad())
            else Redirect(routes.CheckYourAnswersController.onPageLoad())
          }
      )
  }
}

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

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.EoriNumberForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.core.Eori
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.EoriNumberView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EoriNumberController @Inject()(
                                      override val controllerComponents: MessagesControllerComponents,
                                      actionProvider: DeclarationJourneyActionProvider,
                                      override val repo: DeclarationJourneyRepository,
                                      view: EoriNumberView
                                    )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends DeclarationJourneyUpdateController {

  private val backButtonUrl: Call = routes.CustomsAgentController.onPageLoad()

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    request.declarationJourney.maybeIsACustomsAgent.fold(actionProvider.invalidRequest) { isAgent =>
      val preparedForm = request.declarationJourney.maybeEori.fold(form)(e => form.fill(e.value))

      Ok(view(preparedForm, isAgent, backButtonUrl))
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.maybeIsACustomsAgent.fold(actionProvider.invalidRequestF) { isAgent =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, isAgent, backButtonUrl))),
          eori =>
            persistAndRedirect(
              request.declarationJourney.copy(maybeEori = Some(Eori(eori))), routes.TravellerDetailsController.onPageLoad())
        )
    }
  }
}

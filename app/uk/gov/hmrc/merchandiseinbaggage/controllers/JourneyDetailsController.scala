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

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.JourneyDetailsForm.form
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.JourneyDetailsPage
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.merchandiseinbaggage.navigation._

@Singleton
class JourneyDetailsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: JourneyDetailsPage,
  navigator: Navigator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    backToCheckYourAnswersIfCompleteElse(EnterEmailController.onPageLoad)

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    val journeyForm = form(request.declarationType)
    Ok(
      view(
        request.declarationJourney.maybeJourneyDetailsEntry.fold(journeyForm)(details => journeyForm.fill(details)),
        request.declarationType,
        backButtonUrl))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form(request.declarationType)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.declarationType, backButtonUrl))),
        journeyDetailsEntry => {
          val updated = request.declarationJourney.copy(maybeJourneyDetailsEntry = Some(journeyDetailsEntry))
          navigator
            .nextPage(
              JourneyDetailsRequest(
                updated,
                repo.upsert,
                updated.declarationRequiredAndComplete
              ))
            .map(Redirect)
        }
      )
  }
}

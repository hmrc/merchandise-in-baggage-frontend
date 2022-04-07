/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.forms.AgentDetailsForm.form
import uk.gov.hmrc.merchandiseinbaggage.navigation.AgentDetailsRequest
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.AgentDetailsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentDetailsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: AgentDetailsView,
  navigator: Navigator
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    backToCheckYourAnswersIfCompleteElse(routes.CustomsAgentController.onPageLoad)

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    val preparedForm = request.declarationJourney.maybeCustomsAgentName.fold(form)(form.fill)

    Ok(view(preparedForm, backButtonUrl, request.declarationType))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, backButtonUrl, request.declarationType))),
        value => {
          navigator
            .nextPage(AgentDetailsRequest(value, request.declarationJourney, repo.upsert))
            .map(Redirect)
        }
      )
  }
}

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

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.CustomsAgentForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.CustomsAgentView

import scala.concurrent.{ExecutionContext, Future}

class CustomsAgentController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: CustomsAgentView,
  navigator: Navigator
)(implicit ec: ExecutionContext, appConf: AppConfig)
    extends DeclarationJourneyUpdateController {
  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    request.declarationJourney.declarationType match {
      case Import =>
        backToCheckYourAnswersIfCompleteElse(routes.PaymentCalculationController.onPageLoad)
      case Export =>
        backToCheckYourAnswersIfCompleteElse(routes.ReviewGoodsController.onPageLoad)
    }

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(view(request.declarationJourney.maybeIsACustomsAgent.fold(form)(form.fill), request.declarationType, backButtonUrl))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future successful BadRequest(view(formWithErrors, request.declarationType, backButtonUrl)),
        isCustomsAgent => {
          val updatedJourney = request.declarationJourney.copy(maybeIsACustomsAgent = Some(isCustomsAgent))
          navigator
            .nextPage(
              CustomsAgentRequest(isCustomsAgent, updatedJourney, repo.upsert, updatedJourney.declarationRequiredAndComplete)
            )
            .map(Redirect)
        }
      )
  }
}

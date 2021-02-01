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
import play.api.Logging
import play.api.data.FormError
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.forms.EoriNumberForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, Eori, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.EoriNumberView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EoriNumberController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: EoriNumberView,
  mibConnector: MibConnector
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController with Logging {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    backToCheckYourAnswersIfCompleteElse(routes.CustomsAgentController.onPageLoad())

  private val invalidRequestMessage = "maybeIsACustomsAgent is unanswered"

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    import request.declarationJourney._
    maybeIsACustomsAgent.fold(actionProvider.invalidRequest(invalidRequestMessage)) { isAgent =>
      val preparedForm = request.declarationJourney.maybeEori
        .fold(form(isAgent, request.declarationType))(e => form(isAgent, request.declarationType).fill(e.value))

      Ok(view(preparedForm, isAgent, backButtonUrl, declarationType))
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    import request.declarationJourney._
    maybeIsACustomsAgent.fold(actionProvider.invalidRequestF(invalidRequestMessage)) { isAgent =>
      form(isAgent, request.declarationType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, isAgent, backButtonUrl, declarationType))),
          eori => checkEori(eori, isAgent, declarationType)
        )
    }
  }

  private def checkEori(eori: String, isAgent: YesNo, declarationType: DeclarationType)(
    implicit request: DeclarationJourneyRequest[AnyContent]): Future[Result] =
    for {
      validated <- mibConnector.checkEoriNumber(eori)
      result    <- validateEoriAndRedirect(eori, isAgent, declarationType, validated)
    } yield result

  private def validateEoriAndRedirect(eori: String, isAgent: YesNo, declarationType: DeclarationType, response: CheckResponse)(
    implicit request: DeclarationJourneyRequest[AnyContent]): Future[Result] =
    if (response.valid)
      persistAndRedirect(request, eori)
    else {
      val formWithError = form(isAgent, request.declarationType).withError(FormError("eori", "eoriNumber.error.notFound"))
      Future.successful(BadRequest(view(formWithError, isAgent, backButtonUrl, declarationType)))
    }

  private def persistAndRedirect(request: DeclarationJourneyRequest[AnyContent], eori: String): Future[Result] =
    persistAndRedirect(request.declarationJourney.copy(maybeEori = Some(Eori(eori))), routes.TravellerDetailsController.onPageLoad())
}

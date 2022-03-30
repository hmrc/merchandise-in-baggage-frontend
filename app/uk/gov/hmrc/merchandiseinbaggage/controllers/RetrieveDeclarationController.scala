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

import cats.implicits._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.forms.RetrieveDeclarationForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.core.RetrieveDeclaration
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.utils.Utils.FutureOps
import uk.gov.hmrc.merchandiseinbaggage.views.html.RetrieveDeclarationView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveDeclarationController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  mibConnector: MibConnector,
  navigator: Navigator,
  view: RetrieveDeclarationView
)(implicit appConfig: AppConfig, val ec: ExecutionContext)
    extends DeclarationJourneyUpdateController {

  override val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    val preFilledForm = request.declarationJourney.maybeRetrieveDeclaration.fold(form)(form.fill)
    Ok(view(preFilledForm, routes.NewOrExistingController.onPageLoad(), request.declarationJourney.declarationType))
  }

  override val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, routes.NewOrExistingController.onPageLoad(), request.declarationJourney.declarationType)).asFuture,
        retrieveDeclaration => processRequest(retrieveDeclaration)
      )
  }

  private def processRequest(retrieveDeclaration: RetrieveDeclaration)(
    implicit request: DeclarationJourneyRequest[AnyContent],
    hc: HeaderCarrier,
    ec: ExecutionContext) =
    mibConnector
      .findBy(retrieveDeclaration.mibReference, retrieveDeclaration.eori)
      .fold(
        error => Future successful InternalServerError(error), { maybeDeclaration =>
          navigator
            .nextPage(
              RetrieveDeclarationRequest(
                maybeDeclaration,
                request.declarationJourney.copy(maybeRetrieveDeclaration = Some(retrieveDeclaration)),
                repo.upsert))
            .map(Redirect)
        }
      )
      .flatten
}

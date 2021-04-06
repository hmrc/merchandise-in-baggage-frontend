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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.PreviousDeclarationDetailsView

import scala.concurrent.ExecutionContext

@Singleton
class PreviousDeclarationDetailsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  mibConnector: MibConnector,
  navigator: Navigator,
  view: PreviousDeclarationDetailsView)(implicit ec: ExecutionContext, appConf: AppConfig)
    extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    mibConnector.findDeclaration(request.declarationJourney.declarationId).map {
      _.fold(actionProvider.invalidRequest(s"declaration not found for id:${request.declarationJourney.declarationId.value}"))(
        declaration => Ok(view(declaration)))
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    mibConnector.findDeclaration(request.declarationJourney.declarationId).flatMap { maybeOriginalDeclaration =>
      maybeOriginalDeclaration
        .fold(actionProvider.invalidRequestF(s"declaration not found for id:${request.declarationJourney.declarationId.value}")) {
          originalDeclaration =>
            navigator
              .nextPageWithCallBack(PreviousDeclarationDetailsRequest(request.declarationJourney, originalDeclaration, repo.upsert))
              .map(Redirect)
        }
    }
  }
}

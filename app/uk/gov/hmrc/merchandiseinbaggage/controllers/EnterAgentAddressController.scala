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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.connectors.AddressLookupFrontendConnector
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext

@Singleton
class EnterAgentAddressController @Inject()(
                                         override val controllerComponents: MessagesControllerComponents,
                                         actionProvider: DeclarationJourneyActionProvider,
                                         repo: DeclarationJourneyRepository,
                                         addressLookupFrontendConnector: AddressLookupFrontendConnector
                                       )(implicit ec: ExecutionContext)
  extends FrontendBaseController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    addressLookupFrontendConnector.initJourney(routes.EnterAgentAddressController.returnFromAddressLookup())
      .map(Redirect(_))
  }

  def returnFromAddressLookup(id: String): Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    for {
      address <- addressLookupFrontendConnector.getAddress(id)
      _ <- repo.upsert(request.declarationJourney.copy(maybeCustomsAgentAddress = Some(address)))
    } yield
      if (request.declarationJourney.declarationRequiredAndComplete) Redirect(routes.CheckYourAnswersController.onPageLoad())
      else Redirect(routes.EoriNumberController.onPageLoad())
  }
}

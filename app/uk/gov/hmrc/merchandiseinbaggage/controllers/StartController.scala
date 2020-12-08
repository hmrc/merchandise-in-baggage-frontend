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

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, DeclarationType, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository

import scala.concurrent.ExecutionContext

trait StartController extends DeclarationJourneyController {
  val declarationType: DeclarationType
  val repo: DeclarationJourneyRepository
  implicit val ec: ExecutionContext

  val processRequest: Action[AnyContent] = Action.async { implicit request =>
    val sessionId = SessionId()
    repo.insert(DeclarationJourney(sessionId, declarationType)).map { _ =>
      Redirect(routes.GoodsDestinationController.onPageLoad())
        .addingToSession((SessionKeys.sessionId, sessionId.value))
    }
  }
}

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
import uk.gov.hmrc.merchandiseinbaggage.utils.DeclarationJourneyLogger

import scala.concurrent.{ExecutionContext, Future}

trait StartController extends DeclarationJourneyController {
  val declarationType: DeclarationType
  val repo: DeclarationJourneyRepository
  implicit val ec: ExecutionContext

  val onSubmit: Action[AnyContent] = Action.async { implicit request =>
    def next(sessionId: SessionId) = {
      val nextCall = routes.GoodsDestinationController.onPageLoad()
      DeclarationJourneyLogger.info(s"Persisted journey found, redirecting to $nextCall")
      Redirect(nextCall).addingToSession((SessionKeys.sessionId, sessionId.value))
    }

    def newDeclarationJourney(sessionId: SessionId) = {
      val declarationJourney = DeclarationJourney(sessionId = sessionId, declarationType)

      repo.insert(declarationJourney).map { _ =>
        next(sessionId)
      }
    }

    request.session.get(SessionKeys.sessionId) match {
      case None =>
        DeclarationJourneyLogger.info(
          s"StartController. No session so will start a new ${declarationType.entryName} journey")
        newDeclarationJourney(SessionId())

      case Some(id) =>
        val sessionId = SessionId(id)

        repo.findBySessionId(sessionId).flatMap{
          case Some(_) =>
            DeclarationJourneyLogger.info(s"StartController. Persisted journey found")
            Future successful next(sessionId)
          case _ =>
            DeclarationJourneyLogger.info(
              s"StartController. No persisted journey found so will start a new ${declarationType.entryName} journey")
            newDeclarationJourney(sessionId)
        }
    }
  }
}

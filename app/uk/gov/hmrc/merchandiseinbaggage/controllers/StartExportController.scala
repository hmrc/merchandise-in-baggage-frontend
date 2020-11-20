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
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, DeclarationType, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.StartExportView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StartExportController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                      repo: DeclarationJourneyRepository,
                                      view: StartExportView)(implicit val ec: ExecutionContext, appConfig: AppConfig) extends FrontendBaseController {

  def onPageLoad(): Action[AnyContent] = Action { implicit request =>
    Ok(view())
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    def next(sessionId: SessionId) =
      Redirect(routes.GoodsDestinationController.onPageLoad()).addingToSession((SessionKeys.sessionId, sessionId.value))

    def newDeclarationJourney(sessionId: SessionId) = {
      val declarationJourney = DeclarationJourney(sessionId = sessionId, DeclarationType.Export)

      repo.insert(declarationJourney).map { _ =>
        next(sessionId)
      }
    }

    request.session.get(SessionKeys.sessionId) match {
      case None =>
        newDeclarationJourney(SessionId())

      case Some(id) =>
        val sessionId = SessionId(id)

        repo.findBySessionId(sessionId).flatMap{
          case Some(_) => Future successful next(sessionId)
          case _ => newDeclarationJourney(sessionId)
        }
    }
  }
}

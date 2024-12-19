/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.i18n.Messages

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggage.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.{ProgressDeletedView, ServiceTimeoutView}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KeepAliveController @Inject() (
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  strideAction: StrideAuthAction,
  repo: DeclarationJourneyRepository,
  progressDeletedView: ProgressDeletedView,
  serviceTimeoutView: ServiceTimeoutView
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendBaseController {

  implicit def messages(implicit request: Request[?]): Messages = controllerComponents.messagesApi.preferred(request)

  val onKeepAlive: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    repo.upsert(request.declarationJourney).map { _ =>
      NoContent
    }
  }

  // strideAction is only used to determine isAssistedDigital flag for the view
  val onProgressDelete: Action[AnyContent] = strideAction.async { implicit request =>
    removeSession(request)(Ok(progressDeletedView()))
  }

  // strideAction is only used to determine isAssistedDigital flag for the view
  val onServiceTimeout: Action[AnyContent] = strideAction.async { implicit request =>
    removeSession(request)(Ok(serviceTimeoutView()))
  }

  private def removeSession(implicit request: Request[?]): Result => Future[Result] = result =>
    Future.successful(result.removingFromSession(SessionKeys.sessionId))
}

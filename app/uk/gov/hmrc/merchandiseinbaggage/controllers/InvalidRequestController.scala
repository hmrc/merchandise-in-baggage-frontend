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
import uk.gov.hmrc.merchandiseinbaggage.utils.DeclarationJourneyLogger
import uk.gov.hmrc.merchandiseinbaggage.views.html.InvalidRequestView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext

@Singleton
class InvalidRequestController @Inject()(override val controllerComponents: MessagesControllerComponents, view: InvalidRequestView)(
  implicit val ec: ExecutionContext,
  appConfig: AppConfig)
    extends FrontendBaseController {

  def onPageLoad(): Action[AnyContent] = Action { implicit request =>
    DeclarationJourneyLogger.warn(s"User was directed to ${routes.InvalidRequestController.onPageLoad()}")
    Ok(view())
  }
}

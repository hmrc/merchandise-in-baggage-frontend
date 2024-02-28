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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType

import scala.concurrent.ExecutionContext

@Singleton
class StartImportController @Inject() (
  override val controllerComponents: MessagesControllerComponents,
  override val repo: DeclarationJourneyRepository
)(implicit val ec: ExecutionContext)
    extends StartController {

  override val onPageLoad: Action[AnyContent] = Action {
    Redirect(routes.StartImportController.processRequest)
  }

  override val declarationType: DeclarationType = Import
}

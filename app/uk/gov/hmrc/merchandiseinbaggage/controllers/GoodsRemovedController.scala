/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType._
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsRemovedView

@Singleton
class GoodsRemovedController @Inject() (
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  view: GoodsRemovedView
)(implicit val appConfig: AppConfig)
    extends DeclarationJourneyController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    val startAgainUrl =
      if (request.isAssistedDigital) {
        routes.ImportExportChoiceController.onPageLoad.url
      } else {
        backUrl(request)
      }

    Ok(view(startAgainUrl, request.declarationType))
  }

  private def backUrl(request: DeclarationJourneyRequest[AnyContent]): String =
    request.declarationJourney.declarationType match {
      case Import => routes.StartImportController.onPageLoad.url
      case Export => routes.StartExportController.onPageLoad.url
    }
}

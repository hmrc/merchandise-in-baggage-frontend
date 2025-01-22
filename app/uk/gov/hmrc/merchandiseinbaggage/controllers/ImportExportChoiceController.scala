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
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.forms.ImportExportChoiceForm.*
import uk.gov.hmrc.merchandiseinbaggage.model.core.ImportExportChoices.AddToExisting
import uk.gov.hmrc.merchandiseinbaggage.navigation.ImportExportChoiceRequest
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.ImportExportChoice
import uk.gov.hmrc.merchandiseinbaggage.views.html.PageNotFoundView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImportExportChoiceController @Inject() (
  override val controllerComponents: MessagesControllerComponents,
  importExportView: ImportExportChoice,
  pageNotFoundView: PageNotFoundView,
  actionProvider: DeclarationJourneyActionProvider,
  val repo: DeclarationJourneyRepository,
  navigator: Navigator
)(implicit ec: ExecutionContext, appConf: AppConfig)
    extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = actionProvider.initJourneyAction { implicit request =>
    if (!request.isAssistedDigital) {
      Forbidden(pageNotFoundView())
    } else {
      Ok(importExportView(form))
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.initJourneyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future successful BadRequest(importExportView(formWithErrors)),
        choice =>
          navigator
            .nextPage(
              ImportExportChoiceRequest(
                choice,
                SessionId(request.session(SessionKeys.sessionId)),
                repo.upsert,
                request.isAssistedDigital
              )
            )
            .map { call =>
              choice match {
                case AddToExisting => Redirect(call).addingToSession("journeyType" -> "amend")
                case _             => Redirect(call).addingToSession("journeyType" -> "new")
              }
            }
      )
  }
}

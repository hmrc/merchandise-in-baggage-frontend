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

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationType, SessionId}
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.DeclarationConfirmationView

import scala.concurrent.ExecutionContext.Implicits.global

class DeclarationConfirmationControllerSpec extends DeclarationJourneyControllerSpec {

  val repo = app.injector.instanceOf[DeclarationJourneyRepository]
  val view = app.injector.instanceOf[DeclarationConfirmationView]
  val controller = new DeclarationConfirmationController(controllerComponents, actionBuilder, repo, view)

  "on page load return 200" in {
    val sessionId = SessionId()
    givenADeclarationJourneyIsPersisted(completedDeclarationJourney
      .copy(sessionId = sessionId)
      .copy(declarationType = DeclarationType.Export)
    )
    val request = buildGet(routes.CheckYourAnswersController.onPageLoad().url).withSession(SessionKeys.sessionId -> sessionId.value)

    val eventualResult = controller.onPageLoad()(request)

    status(eventualResult) mustBe 200
  }
}

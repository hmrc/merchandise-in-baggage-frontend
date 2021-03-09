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

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationId, DeclarationType, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenPersistedDeclarationIsFound
import uk.gov.hmrc.merchandiseinbaggage.views.html.PreviousDeclarationDetailsView
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.service.DeclarationService

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.{LocalDate, LocalDateTime}

class PreviousDeclarationDetailsControllerSpec extends DeclarationJourneyControllerSpec with WireMockSupport with MibConfiguration {

  "creating a page" should {
    "return 200 if declaration exists and resets the journey" in {
      val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
      val previousDeclarationDetailsService = app.injector.instanceOf[DeclarationService]
      val controller =
        new PreviousDeclarationDetailsController(controllerComponents, actionBuilder, previousDeclarationDetailsService, view)

      val sessionId = SessionId()
      val id = DeclarationId("456")
      val created: LocalDateTime = LocalDate.now.atStartOfDay

      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = DeclarationType.Export, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersisted(exportJourney)

      givenPersistedDeclarationIsFound(exportJourney.declarationIfRequiredAndComplete.get, id)

      val request = buildGet(routes.PreviousDeclarationDetailsController.onPageLoad().url, sessionId)
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe 200

      contentAsString(eventualResult) must include("cheese")

      import exportJourney._
      val resetJourney = DeclarationJourney(sessionId, declarationType)

      declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.sessionId mustBe resetJourney.sessionId
      declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.declarationType mustBe resetJourney.declarationType
    }

    "return 303 if declaration does NOT exist and resets the journey" in {
      val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
      val previousDeclarationDetailsService = app.injector.instanceOf[DeclarationService]
      val controller =
        new PreviousDeclarationDetailsController(controllerComponents, actionBuilder, previousDeclarationDetailsService, view)

      val sessionId = SessionId()
      val id = DeclarationId("456")
      val created = LocalDate.now.atStartOfDay

      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = DeclarationType.Export, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersisted(exportJourney)

      givenPersistedDeclarationIsFound(exportJourney.declarationIfRequiredAndComplete.get, id)

      val request =
        buildGet(routes.PreviousDeclarationDetailsController.onPageLoad().url, SessionId()).withSession("declarationId" -> "987")
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe 303

      contentAsString(eventualResult) mustNot include("cheese")

      import exportJourney._
      val resetJourney = DeclarationJourney(sessionId, declarationType)

      declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.sessionId mustBe resetJourney.sessionId
      declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.declarationType mustBe resetJourney.declarationType
    }
  }
}

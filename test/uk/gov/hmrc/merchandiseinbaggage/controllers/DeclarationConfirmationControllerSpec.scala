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

import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.views.html.DeclarationConfirmationView

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationConfirmationControllerSpec extends DeclarationJourneyControllerSpec {

  private val view       = injector.instanceOf[DeclarationConfirmationView]
  private val client     = injector.instanceOf[HttpClientV2]
  private val connector  = new MibConnector(appConfig, client)
  private val controller =
    new DeclarationConfirmationController(
      controllerComponents,
      actionBuilder,
      view,
      connector,
      declarationJourneyRepository
    )

  "on page load return 200 if declaration exists and resets the journey for Exports" in {
    val sessionId = SessionId()
    val id        = DeclarationId("456")
    val created   = LocalDateTime.now.withSecond(0).withNano(0)
    val request   = buildGet(routes.DeclarationConfirmationController.onPageLoad.url, sessionId)

    val exportJourney: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId, declarationType = DeclarationType.Export, createdAt = created, declarationId = id)

    givenADeclarationJourneyIsPersisted(exportJourney)

    givenPersistedDeclarationIsFound(exportJourney.declarationIfRequiredAndComplete.get, id)

    val eventualResult = controller.onPageLoad()(request)
    val result         = contentAsString(eventualResult)

    status(eventualResult) mustBe OK
    result must include(messages("declarationConfirmation.title"))
    result must include(messages("declarationConfirmation.banner.title"))

    import exportJourney._
    val resetJourney = DeclarationJourney(sessionId, declarationType, isAssistedDigital = false)

    declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.sessionId mustBe resetJourney.sessionId
    declarationJourneyRepository
      .findBySessionId(sessionId)
      .futureValue
      .get
      .declarationType mustBe resetJourney.declarationType
  }

  "on page load return 200 if declaration exists and resets the journey for Import with no payment required" in {
    val sessionId = SessionId()
    val id        = DeclarationId("456")
    val created   = LocalDateTime.now.withSecond(0).withNano(0)
    val request   = buildGet(routes.DeclarationConfirmationController.onPageLoad.url, sessionId)

    val importJourney: DeclarationJourney =
      completedDeclarationJourney.copy(sessionId = sessionId, createdAt = created, declarationId = id)
    val declarationWithNoPaymentRequired  =
      importJourney.declarationIfRequiredAndComplete.get.copy(paymentStatus = Some(NotRequired))

    givenADeclarationJourneyIsPersisted(importJourney)

    givenPersistedDeclarationIsFound(declarationWithNoPaymentRequired, id)

    val eventualResult = controller.onPageLoad()(request)
    val result         = contentAsString(eventualResult)

    status(eventualResult) mustBe OK
    result must include(messages("declarationConfirmation.title"))
    result must include(messages("declarationConfirmation.banner.title"))

    import importJourney._
    val resetJourney = DeclarationJourney(sessionId, declarationType, isAssistedDigital = false)

    declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.sessionId mustBe resetJourney.sessionId
    declarationJourneyRepository
      .findBySessionId(sessionId)
      .futureValue
      .get
      .declarationType mustBe resetJourney.declarationType
  }

  "on page load return 200 return an invalid request for Import with payment required as the confirmation is hosted by payments team" in {
    val sessionId = SessionId()
    val id        = DeclarationId("456")
    val created   = LocalDateTime.now.withSecond(0).withNano(0)
    val request   = buildGet(routes.DeclarationConfirmationController.onPageLoad.url, sessionId)

    val importJourney: DeclarationJourney =
      completedDeclarationJourney.copy(sessionId = sessionId, createdAt = created, declarationId = id)

    givenADeclarationJourneyIsPersisted(importJourney)

    givenPersistedDeclarationIsFound(declarationWithPaidAmendment, id)

    val eventualResult = controller.onPageLoad()(request)
    status(eventualResult) mustBe SEE_OTHER
    redirectLocation(eventualResult) mustBe Some("/declare-commercial-goods/cannot-access-page")
  }

  "on page load return an invalid request if journey is invalidated by resetting" in {
    val connector = new MibConnector(appConfig, client) {
      override def findDeclaration(declarationId: DeclarationId)(implicit
        hc: HeaderCarrier
      ): Future[Option[Declaration]] =
        Future.failed(new Exception("not found"))
    }

    val controller =
      new DeclarationConfirmationController(
        controllerComponents,
        actionBuilder,
        view,
        connector,
        declarationJourneyRepository
      )
    val request    = buildGet(routes.DeclarationConfirmationController.onPageLoad.url, aSessionId)

    val eventualResult = controller.onPageLoad()(request)
    status(eventualResult) mustBe SEE_OTHER
    redirectLocation(eventualResult) mustBe Some("/declare-commercial-goods/cannot-access-page")
  }
}

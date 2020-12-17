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

import java.time.LocalDateTime

import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.views.html.DeclarationConfirmationView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DeclarationConfirmationControllerSpec extends DeclarationJourneyControllerSpec with WireMockSupport with MibConfiguration {

  import mibConf._
  private val view = app.injector.instanceOf[DeclarationConfirmationView]
  private val client = app.injector.instanceOf[HttpClient]
  private val connector = new MibConnector(client, s"$protocol://$host:${WireMockSupport.port}")
  private val controller = new DeclarationConfirmationController(controllerComponents, actionBuilder, view, connector, declarationJourneyRepository)

  "on page load return 200 if declaration exists and resets the journey" in {
    val sessionId = SessionId()
    val id = DeclarationId("456")
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val request = buildGet(routes.DeclarationConfirmationController.onPageLoad().url, sessionId)

    val exportJourney: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId, declarationType = DeclarationType.Export, createdAt = created, declarationId = id)

    givenADeclarationJourneyIsPersisted(exportJourney)

    givenPersistedDeclarationIsFound(wireMockServer, exportJourney.declarationIfRequiredAndComplete.get, id)

    val eventualResult = controller.onPageLoad()(request)
    status(eventualResult) mustBe 200

    import exportJourney._
    val resetJourney = DeclarationJourney(sessionId, declarationType, createdAt = created, declarationId = id)

    declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.copy(createdAt = created) mustBe resetJourney
  }

  "on page load return an invalid request if journey is invalidated by resetting" in {
    val connector = new MibConnector(client, "") {
      override def findDeclaration(declarationId: DeclarationId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Declaration]] =
        Future.failed(new Exception("not found"))
    }

    val controller = new DeclarationConfirmationController(controllerComponents, actionBuilder, view, connector, declarationJourneyRepository)
    val request = buildGet(routes.DeclarationConfirmationController.onPageLoad().url, sessionId)

    val eventualResult = controller.onPageLoad()(request)
    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some("/declare-commercial-goods/invalid-request")
  }
}

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

import java.time.LocalDateTime

import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggagefrontend.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{Declaration, DeclarationJourney, DeclarationType, SessionId}
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.DeclarationConfirmationView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DeclarationConfirmationControllerSpec extends DeclarationJourneyControllerSpec with WireMockSupport with MibConfiguration {

  private val view = app.injector.instanceOf[DeclarationConfirmationView]
  private val repo = injector.instanceOf[DeclarationJourneyRepository]
  private val client = app.injector.instanceOf[HttpClient]
  private val connector = new MibConnector(client, baseUrl) {
    override def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
      Future.successful(HttpResponse(201, ""))
  }
  private val controller = new DeclarationConfirmationController(controllerComponents, actionBuilder, view, repo, connector)

  "on page load return 200 and reset persisted journey declaration if declaration persistence succeed" in {
    val sessionId = SessionId()
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val request = buildGet(routes.DeclarationConfirmationController.onPageLoad().url, sessionId)

    val exportJourney: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId)
      .copy(declarationType = DeclarationType.Export, createdAt = created)

    givenADeclarationJourneyIsPersisted(exportJourney)

    val eventualResult = controller.onPageLoad()(request)
    status(eventualResult) mustBe 200

    import exportJourney._
    val resetJourney = DeclarationJourney(sessionId, declarationType, createdAt = created)

    repo.findBySessionId(sessionId).futureValue.get.copy(createdAt = created) mustBe resetJourney
  }

  "on page load return 500 and do not reset persisted journey declaration if declaration persistence fails" in {
    val connector = new MibConnector(client, baseUrl) {
      override def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
        Future.successful(HttpResponse(500, ""))
    }

    val controller = new DeclarationConfirmationController(controllerComponents, actionBuilder, view, repo, connector)
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val request = buildGet(routes.DeclarationConfirmationController.onPageLoad().url, sessionId)

    val exportJourney: DeclarationJourney = completedDeclarationJourney
      .copy(declarationType = DeclarationType.Export, createdAt = created)

    givenADeclarationJourneyIsPersisted(exportJourney)

    val eventualResult = controller.onPageLoad()(request)
    status(eventualResult) mustBe 500
    repo.findBySessionId(sessionId).futureValue.get.copy(createdAt = created) mustBe exportJourney
  }

  "on page load return an invalid request if journey is invalidated by resetting" in {
    val sessionId = SessionId()
    val request = buildGet(routes.DeclarationConfirmationController.onPageLoad().url, sessionId)

    givenADeclarationJourneyIsPersisted(DeclarationJourney(sessionId, Export))

    val eventualResult = controller.onPageLoad()(request)
    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some("/merchandise-in-baggage/invalid-request")
  }
}

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
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.{ErrorHandler, MibConfiguration}
import uk.gov.hmrc.merchandiseinbaggagefrontend.connectors.{CurrencyConversionConnector, MibConnector, PaymentConnector}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.PayApiRequest
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core._
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.CheckYourAnswersPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends DeclarationJourneyControllerSpec with MibConfiguration {

  private lazy val httpClient = injector.instanceOf[HttpClient]
  private val page = injector.instanceOf[CheckYourAnswersPage]
  private lazy val conversionConnector = injector.instanceOf[CurrencyConversionConnector]
  private implicit lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]
  private val stubbedApiResponse = s"""{"journeyId":"5f3b","nextUrl":"http://host"}"""

  val testPaymentConnector = new PaymentConnector(httpClient, ""){
    override def makePayment(requestBody: PayApiRequest)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
      Future.successful(HttpResponse(201, stubbedApiResponse))
  }

  val testMibConnector = new MibConnector(httpClient, ""){
    override def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationId] =
      Future.successful(DeclarationId("xxx"))
  }

  val stubbedCalculation = new CalculationService(conversionConnector) {
    override def paymentCalculation(declarationGoods: DeclarationGoods)(implicit hc: HeaderCarrier): Future[PaymentCalculations] =
      Future.successful(aPaymentCalculations)
  }

  val controller = new CheckYourAnswersController(controllerComponents, actionBuilder,
    stubbedCalculation, testPaymentConnector, testMibConnector, declarationJourneyRepository, page)

  "on submit will calculate tax and send payment request to pay api and reset declaration journey" in {
    val sessionId = SessionId()
    val id = DeclarationId("xxx")
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val importJourney: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId)
      .copy(declarationType = DeclarationType.Import, createdAt = created, declarationId = Some(id))

    givenADeclarationJourneyIsPersisted(importJourney)

    val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, sessionId)
    val eventualResult = controller.onSubmit()(request)

    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some("http://host")

    import importJourney._
    val resetJourney = DeclarationJourney(sessionId, declarationType, createdAt = created, declarationId = Some(id))

    declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.copy(createdAt = created) mustBe resetJourney
  }

  "on submit will redirect to declaration-confirmation if exporting and reset declaration journey" in {
    val sessionId = SessionId()
    val stubbedId = DeclarationId("xxx")
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val exportJourney: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId)
      .copy(declarationType = DeclarationType.Export, createdAt = created, declarationId = Some(stubbedId))

    val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, sessionId)

    givenADeclarationJourneyIsPersisted(exportJourney)

    val eventualResult = controller.onSubmit()(request)

    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad().url)

    import exportJourney._
    val resetJourney = DeclarationJourney(sessionId, declarationType, createdAt = created, declarationId = Some(stubbedId))

    declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.copy(createdAt = created) mustBe resetJourney
  }

  "on submit will redirect to invalid request when redirected from declaration confirmation with journey reset" in {
    val declarationJourney = startedExportJourney
    val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, sessionId)

    givenADeclarationJourneyIsPersisted(declarationJourney)

    val eventualResult = controller.onSubmit()(request)

    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some(routes.InvalidRequestController.onPageLoad().url)
  }
}

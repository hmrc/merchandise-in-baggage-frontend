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
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.{CurrencyConversionConnector, MibConnector, PaymentConnector}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, JourneyId, PayApiRequest, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersExportView, CheckYourAnswersImportView}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends DeclarationJourneyControllerSpec with MibConfiguration {

  private lazy val httpClient = injector.instanceOf[HttpClient]
  private lazy val importView = injector.instanceOf[CheckYourAnswersImportView]
  private lazy val exportView = injector.instanceOf[CheckYourAnswersExportView]
  private lazy val conversionConnector = injector.instanceOf[CurrencyConversionConnector]

  private lazy val testPaymentConnector = new PaymentConnector(httpClient, "") {
    override def sendPaymentRequest(requestBody: PayApiRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PayApiResponse] =
      Future.successful(PayApiResponse(JourneyId("5f3b"), URL("http://host")))
  }

  private lazy val testMibConnector = new MibConnector(httpClient, "") {
    override def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationId] =
      Future.successful(DeclarationId("xxx"))

    override def sendEmails(declarationId: DeclarationId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
      Future.successful(())
  }

  private lazy val stubbedCalculation = new CalculationService(conversionConnector) {
    override def paymentCalculation(declarationGoods: DeclarationGoods)(implicit hc: HeaderCarrier): Future[PaymentCalculations] =
      Future.successful(aPaymentCalculations)
  }

  private lazy val controller = new CheckYourAnswersController(
    controllerComponents,
    actionBuilder,
    stubbedCalculation,
    testPaymentConnector,
    testMibConnector,
    declarationJourneyRepository,
    importView,
    exportView)

  "on submit will calculate tax and send payment request to pay api" in {
    val sessionId = SessionId()
    val id = DeclarationId("xxx")
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val importJourney: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId, declarationType = Import, createdAt = created, declarationId = id)

    givenADeclarationJourneyIsPersisted(importJourney)

    val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, sessionId)
    val eventualResult = controller.onSubmit()(request)

    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some("http://host")
  }

  "on submit will redirect to declaration-confirmation if exporting" in {
    val sessionId = SessionId()
    val stubbedId = DeclarationId("xxx")
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val exportJourney: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId, declarationType = Export, createdAt = created, declarationId = stubbedId)

    val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, sessionId)

    givenADeclarationJourneyIsPersisted(exportJourney)

    val eventualResult = controller.onSubmit()(request)

    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad().url)
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

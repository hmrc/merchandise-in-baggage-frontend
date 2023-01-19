/*
 * Copyright 2023 HM Revenue & Customs
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

import com.softwaremill.quicklens._
import org.scalamock.scalatest.MockFactory
import play.api.mvc.Request
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.{MibConnector, PaymentConnector}
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, CalculationResults, OverThreshold, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{JourneyId, PayApiRequest, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, URL}
import uk.gov.hmrc.merchandiseinbaggage.model.tpspayments.TpsId
import uk.gov.hmrc.merchandiseinbaggage.service.{MibService, PaymentService, TpsPaymentsService}
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersExportView, CheckYourAnswersImportView}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersNewHandlerSpec extends DeclarationJourneyControllerSpec with MibConfiguration with MockFactory {

  private lazy val httpClient = injector.instanceOf[HttpClient]
  private lazy val mockTpsPaymentsService = mock[TpsPaymentsService]
  private lazy val importView = injector.instanceOf[CheckYourAnswersImportView]
  private lazy val exportView = injector.instanceOf[CheckYourAnswersExportView]
  private lazy val mibConnector = injector.instanceOf[MibConnector]
  private val auditConnector = injector.instanceOf[AuditConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val testPaymentConnector = new PaymentConnector(httpClient, "") {
    override def sendPaymentRequest(requestBody: PayApiRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PayApiResponse] =
      Future.successful(payapi.PayApiResponse(JourneyId("5f3b"), URL("http://host")))
  }

  private lazy val testMibConnector = new MibConnector(httpClient, "") {
    override def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier): Future[DeclarationId] =
      Future.successful(DeclarationId("abc"))
  }

  private lazy val stubbedCalculation: CalculationResponse => MibService = calculationResponse =>
    new MibService(mibConnector) {
      override def paymentCalculations(goods: Seq[Goods], destination: GoodsDestination)(
        implicit hc: HeaderCarrier): Future[CalculationResponse] =
        Future.successful(calculationResponse)
  }

  private def newHandler(paymentCalcs: CalculationResponse = aCalculationResponse) =
    new CheckYourAnswersNewHandler(
      stubbedCalculation(paymentCalcs),
      mockTpsPaymentsService,
      new PaymentService(testPaymentConnector, auditConnector, messagesApi),
      testMibConnector,
      importView,
      exportView,
    )

  declarationTypes.foreach { importOrExport: DeclarationType =>
    "onPageLoad" should {
      s"return Ok with correct page content for $importOrExport" in {
        val sessionId = SessionId()
        val id = DeclarationId("xxx")
        val created = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(sessionId = sessionId, declarationType = importOrExport, createdAt = created, declarationId = id)

        givenADeclarationJourneyIsPersisted(journey)

        implicit val request: Request[_] = buildGet(CheckYourAnswersController.onPageLoad.url, sessionId)

        val eventualResult = newHandler().onPageLoad(declaration, YesNo.Yes)

        status(eventualResult) mustBe OK
        contentAsString(eventualResult) must include(messageApi("checkYourAnswers.title"))
      }

      s"return 303 for goods over threshold for $importOrExport" in {
        val sessionId = SessionId()
        val id = DeclarationId("xxx")
        val created = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(sessionId = sessionId, declarationType = importOrExport, createdAt = created, declarationId = id)

        givenADeclarationJourneyIsPersisted(journey)

        val overThresholdGoods = aCalculationResponse
          .modify(_.thresholdCheck)
          .setTo(OverThreshold)

        implicit val request: Request[_] = buildGet(CheckYourAnswersController.onPageLoad.url, sessionId)

        val eventualResult = newHandler(overThresholdGoods).onPageLoad(declaration, YesNo.Yes)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(GoodsOverThresholdController.onPageLoad.url)
      }
    }
  }

  "on submit" should {
    "will calculate tax and send payment request to pay api for Imports" in {
      val sessionId = SessionId()
      val id = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersisted(importJourney)

      val eventualResult = newHandler().onSubmit(declaration)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some("http://host")
    }

    "will calculate tax and send payment request to TPS for Imports" in {
      val sessionId = SessionId()
      implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad.url, sessionId)
      val id = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersistedWithStub(importJourney)

      (mockTpsPaymentsService
        .createTpsPayments(_: String, _: Option[Int], _: Declaration, _: CalculationResults)(_: HeaderCarrier))
        .expects("123", None, *, *, *)
        .returning(Future.successful(TpsId("someid")))
        .once()

      val eventualResult = newHandler().onSubmit(importJourney.toDeclaration, "123")

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some("http://localhost:9124/tps-payments/make-payment/mib/someid")
    }

    "will redirect to confirmation if totalTax is £0 and should not call pay api" in {
      val sessionId = SessionId()
      val id = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersisted(importJourney)

      val eventualResult = newHandler(CalculationResponse(aCalculationResultsWithNoTax, WithinThreshold)).onSubmit(declaration)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(DeclarationConfirmationController.onPageLoad.url)
    }

    "will redirect to declaration-confirmation for Export" in {
      val sessionId = SessionId()
      val stubbedId = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = Export, createdAt = created, declarationId = stubbedId)

      givenADeclarationJourneyIsPersisted(exportJourney)

      val eventualResult = newHandler().onSubmit(declaration.copy(declarationType = Export))

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(DeclarationConfirmationController.onPageLoad.url)
    }

  }
}

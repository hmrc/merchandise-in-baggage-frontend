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

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, when}
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.{MibConnector, PaymentConnector}
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, CalculationResults, OverThreshold, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{JourneyId, PayApiRequest, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, URL}
import uk.gov.hmrc.merchandiseinbaggage.service.{MibService, PaymentService, TpsPaymentsService}
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersExportView, CheckYourAnswersImportView}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersNewHandlerSpec extends DeclarationJourneyControllerSpec {

  private lazy val httpClient: HttpClientV2                   = injector.instanceOf[HttpClientV2]
  private lazy val importView: CheckYourAnswersImportView     = injector.instanceOf[CheckYourAnswersImportView]
  private lazy val exportView: CheckYourAnswersExportView     = injector.instanceOf[CheckYourAnswersExportView]
  private lazy val mibConnector: MibConnector                 = injector.instanceOf[MibConnector]
  private val auditConnector: AuditConnector                  = injector.instanceOf[AuditConnector]
  private lazy val mockTpsPaymentsService: TpsPaymentsService = mock(classOf[TpsPaymentsService])

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val testPaymentConnector: PaymentConnector = new PaymentConnector(appConfig, httpClient) {
    override def sendPaymentRequest(
      requestBody: PayApiRequest
    )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PayApiResponse] =
      Future.successful(payapi.PayApiResponse(JourneyId("5f3b"), URL("http://host")))
  }

  private lazy val testMibConnector: MibConnector = new MibConnector(appConfig, httpClient) {
    override def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier): Future[DeclarationId] =
      Future.successful(DeclarationId("abc"))
  }

  private lazy val stubbedCalculation: CalculationResponse => MibService = calculationResponse =>
    new MibService(mibConnector) {
      override def paymentCalculations(goods: Seq[Goods], destination: GoodsDestination)(implicit
        hc: HeaderCarrier
      ): Future[CalculationResponse] =
        Future.successful(calculationResponse)
    }

  private def newHandler(paymentCalcs: CalculationResponse = aCalculationResponse): CheckYourAnswersNewHandler =
    new CheckYourAnswersNewHandler(
      stubbedCalculation(paymentCalcs),
      mockTpsPaymentsService,
      new PaymentService(testPaymentConnector, auditConnector),
      testMibConnector,
      importView,
      exportView
    )

  declarationTypes.foreach { (importOrExport: DeclarationType) =>
    "onPageLoad" should {
      s"return Ok with correct page content for $importOrExport" in {
        val sessionId                   = SessionId()
        val id                          = DeclarationId("xxx")
        val created                     = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(sessionId = sessionId, declarationType = importOrExport, createdAt = created, declarationId = id)

        givenADeclarationJourneyIsPersisted(journey)

        implicit val request: DeclarationJourneyRequest[?] =
          buildGet(CheckYourAnswersController.onPageLoad.url, sessionId)

        val result = newHandler().onPageLoad(declaration, YesNo.Yes)

        status(result) mustBe OK
        contentAsString(result) must include(messages("checkYourAnswers.title"))
      }

      s"return 303 for goods over threshold for $importOrExport" in {
        val sessionId                   = SessionId()
        val id                          = DeclarationId("xxx")
        val created                     = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(sessionId = sessionId, declarationType = importOrExport, createdAt = created, declarationId = id)

        givenADeclarationJourneyIsPersisted(journey)

        val overThresholdGoods = aCalculationResponse.copy(thresholdCheck = OverThreshold)

        implicit val request: DeclarationJourneyRequest[?] =
          buildGet(CheckYourAnswersController.onPageLoad.url, sessionId)

        val result = newHandler(overThresholdGoods).onPageLoad(declaration, YesNo.Yes)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(GoodsOverThresholdController.onPageLoad.url)
      }
    }
  }

  "on submit" should {
    "will calculate tax and send payment request to pay api for Imports" in {
      val sessionId                         = SessionId()
      val id                                = DeclarationId("xxx")
      val created                           = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersisted(importJourney)

      val result: Future[Result] = newHandler().onSubmit(declaration)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("http://host")
    }

    "will calculate tax and send payment request to TPS for Imports" in {
      val sessionId                         = SessionId()
      implicit val request: Request[?]      = buildGet(routes.CheckYourAnswersController.onPageLoad.url, sessionId)
      val id                                = DeclarationId("xxx")
      val created                           = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersistedWithStub(importJourney)

      when(
        mockTpsPaymentsService.createTpsPayments(eqTo(None), any[Declaration], any[CalculationResults])(
          any[HeaderCarrier]
        )
      )
        .thenReturn(
          Future.successful(
            PayApiResponse(JourneyId("someid"), URL("http://localhost:9124/tps-payments/make-payment/mib/someid"))
          )
        )

      val result: Future[Result] = newHandler().onSubmitTps(importJourney.toDeclaration)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("http://localhost:9124/tps-payments/make-payment/mib/someid")
    }

    "will redirect to confirmation if totalTax is £0 and should not call pay api" in {
      val sessionId                         = SessionId()
      val id                                = DeclarationId("xxx")
      val created                           = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersisted(importJourney)

      val result: Future[Result] =
        newHandler(CalculationResponse(aCalculationResultsWithNoTax, WithinThreshold)).onSubmit(declaration)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(DeclarationConfirmationController.onPageLoad.url)
    }

    "will redirect to declaration-confirmation for Export" in {
      val sessionId                         = SessionId()
      val stubbedId                         = DeclarationId("xxx")
      val created                           = LocalDateTime.now.withSecond(0).withNano(0)
      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = Export, createdAt = created, declarationId = stubbedId)

      givenADeclarationJourneyIsPersisted(exportJourney)

      val result: Future[Result] = newHandler().onSubmit(declaration.copy(declarationType = Export))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(DeclarationConfirmationController.onPageLoad.url)
    }
  }
}

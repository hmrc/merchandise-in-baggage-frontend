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

import java.time.LocalDateTime
import com.softwaremill.quicklens._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.{MibConnector, PaymentConnector}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResult, CalculationResults}
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{JourneyId, PayApiRequest, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationId, payapi, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, URL}
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, PaymentService}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView, CheckYourAnswersExportView, CheckYourAnswersImportView}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends DeclarationJourneyControllerSpec with MibConfiguration with WireMockSupport {

  private lazy val httpClient = injector.instanceOf[HttpClient]
  private lazy val importView = injector.instanceOf[CheckYourAnswersImportView]
  private lazy val exportView = injector.instanceOf[CheckYourAnswersExportView]
  private lazy val amendImportView = injector.instanceOf[CheckYourAnswersAmendImportView]
  private lazy val amendExportView = injector.instanceOf[CheckYourAnswersAmendExportView]
  private lazy val mibConnector = injector.instanceOf[MibConnector]

  private lazy val testPaymentConnector = new PaymentConnector(httpClient, "") {
    override def sendPaymentRequest(requestBody: PayApiRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PayApiResponse] =
      Future.successful(payapi.PayApiResponse(JourneyId("5f3b"), URL("http://host")))
  }

  private lazy val testMibConnector = new MibConnector(httpClient, "") {
    override def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier): Future[DeclarationId] =
      Future.successful(DeclarationId("xxx"))
  }

  private lazy val stubbedCalculation: CalculationResults => CalculationService = aPaymentCalculations =>
    new CalculationService(mibConnector) {
      override def paymentCalculations(importGoods: Seq[ImportGoods])(implicit hc: HeaderCarrier): Future[CalculationResults] =
        Future.successful(aPaymentCalculations)
  }

  private def controller(paymentCalcs: CalculationResults = aCalculationResults) = new CheckYourAnswersController(
    controllerComponents,
    actionBuilder,
    stubbedCalculation(paymentCalcs),
    new PaymentService(testPaymentConnector),
    testMibConnector,
    declarationJourneyRepository,
    importView,
    exportView,
    amendImportView,
    amendExportView
  )

  "on submit" should {
    "will calculate tax and send payment request to pay api" in {
      val sessionId = SessionId()
      val id = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = Import, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersisted(importJourney)

      val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, sessionId)
      val eventualResult = controller().onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some("http://host")
    }

    "will redirect to confirmation if totalTax is £0 and should not call pay api" in {
      val sessionId = SessionId()
      val id = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = Import, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersisted(importJourney)

      val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, sessionId)
      val eventualResult = controller(aCalculationResultsWithNoTax).onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad().url)
    }

    "will redirect to declaration-confirmation if exporting" in {
      val sessionId = SessionId()
      val stubbedId = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = Export, createdAt = created, declarationId = stubbedId)

      val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, sessionId)

      givenADeclarationJourneyIsPersisted(exportJourney)

      val eventualResult = controller().onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad().url)
    }

    s"will redirect to ${routes.GoodsOverThresholdController.onPageLoad().url} if over threshold" in {
      val sessionId = SessionId()
      val stubbedId = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = Import, createdAt = created, declarationId = stubbedId)

      val request = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

      givenADeclarationJourneyIsPersisted(exportJourney)
      givenAPaymentCalculation(CalculationResult(aImportGoods, AmountInPence(0), AmountInPence(0), AmountInPence(0), None))

      val eventualResult = controller(
        aCalculationResults
          .modify(_.calculationResults.each)
          .setTo(aCalculationResult.modify(_.gbpAmount).setTo(AmountInPence(150000001)))).onPageLoad()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.GoodsOverThresholdController.onPageLoad().url)
    }

    "will redirect to invalid request when redirected from declaration confirmation with journey reset" in {
      val declarationJourney = startedExportJourney
      val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, aSessionId)

      givenADeclarationJourneyIsPersisted(declarationJourney)

      val eventualResult = controller().onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.CannotAccessPageController.onPageLoad().url)
    }
  }
}

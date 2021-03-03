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

import com.softwaremill.quicklens._
import play.api.mvc.Request
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.{MibConnector, PaymentConnector}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResults
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{JourneyId, PayApiRequest, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationId, payapi, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, URL}
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, PaymentService}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenPersistedDeclarationIsFound
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersAmendHandlerSpec extends DeclarationJourneyControllerSpec with MibConfiguration with WireMockSupport {

  private lazy val httpClient = injector.instanceOf[HttpClient]
  private lazy val importView = injector.instanceOf[CheckYourAnswersAmendImportView]
  private lazy val exportView = injector.instanceOf[CheckYourAnswersAmendExportView]
  private lazy val mibConnector = injector.instanceOf[MibConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val testPaymentConnector = new PaymentConnector(httpClient, "") {
    override def sendPaymentRequest(requestBody: PayApiRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PayApiResponse] =
      Future.successful(payapi.PayApiResponse(JourneyId("5f3b"), URL("http://host")))
  }

  private lazy val stubbedCalculation: CalculationResults => CalculationService = aPaymentCalculations =>
    new CalculationService(mibConnector) {
      override def paymentCalculations(importGoods: Seq[ImportGoods])(implicit hc: HeaderCarrier): Future[CalculationResults] =
        Future.successful(aPaymentCalculations)
  }

  private def amendHandler(paymentCalcs: CalculationResults = aCalculationResults) =
    new CheckYourAnswersAmendHandler(
      actionBuilder,
      new PaymentService(testPaymentConnector),
      stubbedCalculation(paymentCalcs),
      mibConnector,
      importView,
      exportView,
    )

  declarationTypes.foreach { importOrExport: DeclarationType =>
    "onPageLoad" should {
      s"return Ok with correct page content for $importOrExport" in {
        val sessionId = SessionId()
        val id = DeclarationId("abc")
        val created = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(sessionId = sessionId, declarationType = importOrExport, createdAt = created, declarationId = id)

        givenADeclarationJourneyIsPersisted(journey)
        givenPersistedDeclarationIsFound(declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult)), id)

        implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

        val amendment = completedAmendment(importOrExport)

        val eventualResult = amendHandler().onPageLoad(importOrExport, amendment, journey.declarationId)

        status(eventualResult) mustBe OK
        contentAsString(eventualResult) must include(messageApi("checkYourAnswers.title"))
      }

      s"return 303 for goods over threshold for $importOrExport" in {
        val sessionId = SessionId()
        val id = DeclarationId("xxx")
        val created = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(
            sessionId = sessionId,
            declarationType = importOrExport,
            createdAt = created,
            declarationId = id,
            goodsEntries = overThresholdGoods(importOrExport))

        val declaration = journey.declarationIfRequiredAndComplete.get
        val exportOverThresholdDeclaration = declaration.copy(
          maybeTotalCalculationResult = Some(aTotalCalculationResult.modify(_.totalGbpValue).setTo(AmountInPence(150000001))))

        givenADeclarationJourneyIsPersisted(journey)
        givenPersistedDeclarationIsFound(exportOverThresholdDeclaration, id)

        val amendment = completedAmendment(importOrExport)

        implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

        val importOverThresholdGoods = aCalculationResults
          .modify(_.calculationResults.each)
          .setTo(aCalculationResult.modify(_.gbpAmount).setTo(AmountInPence(150000001)))

        val eventualResult = amendHandler(importOverThresholdGoods).onPageLoad(importOrExport, amendment, journey.declarationId)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.GoodsOverThresholdController.onPageLoad().url)
      }
    }
  }
}

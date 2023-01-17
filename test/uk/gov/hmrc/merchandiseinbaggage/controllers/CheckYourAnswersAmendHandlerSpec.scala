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

import java.time.LocalDateTime

import org.scalamock.scalatest.MockFactory
import play.api.mvc.Request
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, CalculationResults, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationId, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.model.tpspayments.TpsId
import uk.gov.hmrc.merchandiseinbaggage.service.{MibService, PaymentService, TpsPaymentsService}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.{givenAnAmendPaymentCalculations, givenDeclarationIsAmendedInBackend, givenPersistedDeclarationIsFound}
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersAmendHandlerSpec
    extends DeclarationJourneyControllerSpec with MibConfiguration with WireMockSupport with MockFactory with PropertyBaseTables {

  private val importView = injector.instanceOf[CheckYourAnswersAmendImportView]
  private val exportView = injector.instanceOf[CheckYourAnswersAmendExportView]
  private val mibConnector = injector.instanceOf[MibConnector]
  private val mockTpsPaymentsService = mock[TpsPaymentsService]
  private val paymentService = mock[PaymentService]
  private val mockMibService = mock[MibService]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val stubbedCalculation: CalculationResults => MibService = _ =>
    new MibService(mibConnector) {
      override def paymentCalculations(goods: Seq[Goods], destination: GoodsDestination)(
        implicit hc: HeaderCarrier): Future[CalculationResponse] =
        Future.successful(aCalculationResponse)
  }

  private def amendHandler(paymentCalcs: CalculationResults = aCalculationResults) =
    new CheckYourAnswersAmendHandler(
      actionBuilder,
      stubbedCalculation(paymentCalcs),
      mockTpsPaymentsService,
      paymentService,
      importView,
      exportView,
    )

  forAll(declarationTypesTable) { importOrExport: DeclarationType =>
    "onPageLoad" should {
      s"return Ok with correct page content for $importOrExport" in {
        val sessionId = SessionId()
        val id = DeclarationId("abc")
        val created = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(sessionId = sessionId, declarationType = importOrExport, createdAt = created, declarationId = id, journeyType = Amend)

        givenAnAmendPaymentCalculations(Seq(aCalculationResult), WithinThreshold)
        givenADeclarationJourneyIsPersisted(journey)
        givenPersistedDeclarationIsFound(declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult)), id)

        implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad.url, sessionId)

        val amendment = completedAmendment(importOrExport)

        val eventualResult = amendHandler().onPageLoad(journey, amendment, YesNo.No)

        status(eventualResult) mustBe OK
        contentAsString(eventualResult) must include(messageApi("checkYourAnswers.amend.title"))
      }

      s"will calculate tax and send payment request to TPS for $importOrExport" in {
        val sessionId = SessionId()
        implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad.url, sessionId)
        val id = DeclarationId("xxx")
        val created = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(sessionId = sessionId, createdAt = created, declarationId = id, declarationType = importOrExport)
        val handler = new CheckYourAnswersAmendHandler(
          actionBuilder,
          mockMibService,
          mockTpsPaymentsService,
          paymentService,
          importView,
          exportView,
        )

        givenADeclarationJourneyIsPersistedWithStub(journey)

        (mockMibService
          .findDeclaration(_: DeclarationId)(_: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(Some(journey.toDeclaration)))

        if (importOrExport == Import) {
          (mockMibService
            .paymentCalculations(_: Seq[Goods], _: GoodsDestination)(_: HeaderCarrier))
            .expects(*, *, *)
            .returning(Future.successful(aCalculationResponse))
        }

        (mockMibService
          .amendDeclaration(_: Declaration)(_: HeaderCarrier))
          .expects(*, *)
          .returning(Future.successful(journey.declarationId))

        if (importOrExport == Import) {
          (mockTpsPaymentsService
            .createTpsPayments(_: String, _: Option[Int], _: Declaration, _: CalculationResults)(_: HeaderCarrier))
            .expects("123", Some(1), *, *, *)
            .returning(Future.successful(TpsId("someid")))
        }

        val amendment = completedAmendment(Import)
        val eventualResult = handler.onSubmit(journey.toDeclaration.declarationId, "123", amendment)

        status(eventualResult) mustBe 303
        if (importOrExport == Export) redirectLocation(eventualResult) mustBe Some("/declare-commercial-goods/declaration-confirmation")
        if (importOrExport == Import)
          redirectLocation(eventualResult) mustBe Some("http://localhost:9124/tps-payments/make-payment/mib/someid")
      }
    }
  }

  "will redirect to declaration-confirmation for Export" in {
    val sessionId = SessionId()
    val stubbedId = DeclarationId("xxx")
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val exportJourney: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId, declarationType = Export, createdAt = created, declarationId = stubbedId)

    givenPersistedDeclarationIsFound(declaration.copy(declarationType = Export, declarationId = stubbedId), stubbedId)
    givenADeclarationJourneyIsPersisted(exportJourney)
    givenDeclarationIsAmendedInBackend

    val newAmendment = completedAmendment(Export)

    implicit val request: Request[_] = buildPost(routes.CheckYourAnswersController.onPageLoad.url, sessionId)

    val eventualResult = amendHandler().onSubmit(stubbedId, newAmendment)

    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad.url)
  }
}

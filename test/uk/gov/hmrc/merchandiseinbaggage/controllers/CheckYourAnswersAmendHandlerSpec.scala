/*
 * Copyright 2024 HM Revenue & Customs
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

import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.{mock, when}
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, CalculationResults, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{JourneyId, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, URL}
import uk.gov.hmrc.merchandiseinbaggage.service.{MibService, PaymentService, TpsPaymentsService}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersAmendHandlerSpec extends DeclarationJourneyControllerSpec with PropertyBaseTables {

  private val importView: CheckYourAnswersAmendImportView = injector.instanceOf[CheckYourAnswersAmendImportView]
  private val exportView: CheckYourAnswersAmendExportView = injector.instanceOf[CheckYourAnswersAmendExportView]
  private val mibConnector: MibConnector                  = injector.instanceOf[MibConnector]
  private val mockTpsPaymentsService: TpsPaymentsService  = mock[TpsPaymentsService]
  private val paymentService: PaymentService              = mock[PaymentService]
  private val mockMibService: MibService                  = mock[MibService]

  private val sessionId: SessionId = SessionId()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val stubbedCalculation: CalculationResults => MibService = _ =>
    new MibService(mibConnector) {
      override def paymentCalculations(goods: Seq[Goods], destination: GoodsDestination)(implicit
        hc: HeaderCarrier
      ): Future[CalculationResponse] =
        Future.successful(aCalculationResponse)
    }

  private def amendHandler(paymentCalcs: CalculationResults = aCalculationResults) =
    new CheckYourAnswersAmendHandler(
      actionBuilder,
      stubbedCalculation(paymentCalcs),
      mockTpsPaymentsService,
      paymentService,
      importView,
      exportView
    )

  forAll(declarationTypesTable) { importOrExport: DeclarationType =>
    "onPageLoad" should {
      s"return Ok with correct page content for $importOrExport" in {
        val id                          = DeclarationId("abc")
        val created                     = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(
            sessionId = sessionId,
            declarationType = importOrExport,
            createdAt = created,
            declarationId = id,
            journeyType = Amend
          )

        givenAnAmendPaymentCalculations(Seq(aCalculationResult), WithinThreshold)
        givenADeclarationJourneyIsPersisted(journey)
        givenPersistedDeclarationIsFound(
          declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult)),
          id
        )

        implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad.url, sessionId)

        val amendment = completedAmendment(importOrExport)

        val eventualResult = amendHandler().onPageLoad(journey, amendment, YesNo.No)

        status(eventualResult) mustBe OK
        contentAsString(eventualResult) must include(messageApi("checkYourAnswers.amend.title"))
      }

      s"will calculate tax and send payment request to TPS for $importOrExport" in {

        implicit val request: Request[_]          = buildGet(routes.CheckYourAnswersController.onPageLoad.url, sessionId)
        val id: DeclarationId                     = DeclarationId("xxx")
        val created: LocalDateTime                = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney           = completedDeclarationJourney
          .copy(sessionId = sessionId, createdAt = created, declarationId = id, declarationType = importOrExport)
        val handler: CheckYourAnswersAmendHandler = new CheckYourAnswersAmendHandler(
          actionBuilder,
          mockMibService,
          mockTpsPaymentsService,
          paymentService,
          importView,
          exportView
        )

        givenADeclarationJourneyIsPersistedWithStub(journey)

        when(mockMibService.findDeclaration(any[DeclarationId])(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(journey.toDeclaration)))
        when(mockMibService.amendDeclaration(any[Declaration])(any[HeaderCarrier]))
          .thenReturn(Future.successful(journey.declarationId))

        if (importOrExport == Import) {
          when(mockMibService.paymentCalculations(any[Seq[Goods]], any[GoodsDestination])(any[HeaderCarrier]))
            .thenReturn(Future.successful(aCalculationResponse))
          when(
            mockTpsPaymentsService.createTpsPayments(
              eqTo(Some(1)),
              any[Declaration],
              any[CalculationResults]
            )(any[HeaderCarrier])
          )
            .thenReturn(Future.successful(PayApiResponse(JourneyId("someid"), URL("url"))))
        }

        val amendment: Amendment   = completedAmendment(Import)
        val result: Future[Result] = handler.onSubmitTps(journey.toDeclaration.declarationId, amendment)

        status(result) mustBe SEE_OTHER

        if (importOrExport == Export) {
          redirectLocation(result) mustBe Some("/declare-commercial-goods/declaration-confirmation")
        } else {
          redirectLocation(result) mustBe Some("url")
        }
      }
    }
  }

  "will redirect to declaration-confirmation for Export" in {
    val stubbedId                         = DeclarationId("xxx")
    val created                           = LocalDateTime.now.withSecond(0).withNano(0)
    val exportJourney: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId, declarationType = Export, createdAt = created, declarationId = stubbedId)

    givenPersistedDeclarationIsFound(
      declaration.copy(declarationType = Export, declarationId = stubbedId),
      stubbedId
    )
    givenADeclarationJourneyIsPersisted(exportJourney)
    givenDeclarationIsAmendedInBackend

    val newAmendment = completedAmendment(Export)

    implicit val request: Request[_] = buildPost(routes.CheckYourAnswersController.onPageLoad.url, sessionId)

    val eventualResult = amendHandler().onSubmit(stubbedId, newAmendment)

    status(eventualResult) mustBe SEE_OTHER
    redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad.url)
  }
}

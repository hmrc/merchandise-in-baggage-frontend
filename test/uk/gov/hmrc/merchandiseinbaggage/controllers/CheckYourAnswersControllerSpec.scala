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

import cats.data.OptionT
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, when}
import play.api.mvc.Results._
import play.api.mvc.{Request, RequestHeader, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.connectors.{MibConnector, PaymentConnector}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{JourneyId, PayApiRequest, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, URL}
import uk.gov.hmrc.merchandiseinbaggage.service.{MibService, PaymentService, TpsPaymentsService}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.views.html._
import uk.gov.hmrc.merchandiseinbaggage.wiremock.MockStrideAuth._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends DeclarationJourneyControllerSpec {

  private val httpClient: HttpClient = injector.instanceOf[HttpClient]

  private val importView: CheckYourAnswersImportView           = injector.instanceOf[CheckYourAnswersImportView]
  private val exportView: CheckYourAnswersExportView           = injector.instanceOf[CheckYourAnswersExportView]
  private val amendImportView: CheckYourAnswersAmendImportView = injector.instanceOf[CheckYourAnswersAmendImportView]
  private val amendExportView: CheckYourAnswersAmendExportView = injector.instanceOf[CheckYourAnswersAmendExportView]

  private val mibConnector: MibConnector     = injector.instanceOf[MibConnector]
  private val auditConnector: AuditConnector = injector.instanceOf[AuditConnector]

  private val mockMibService: MibService         = mock[MibService]
  private val mockTpsService: TpsPaymentsService = mock[TpsPaymentsService]

  private lazy val testPaymentConnector: PaymentConnector = new PaymentConnector(appConfig, httpClient) {
    override def sendPaymentRequest(
      requestBody: PayApiRequest
    )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PayApiResponse] =
      Future.successful(payapi.PayApiResponse(JourneyId("5f3b"), URL("http://host")))
  }

  private def newHandler(): CheckYourAnswersNewHandler =
    new CheckYourAnswersNewHandler(
      mockMibService,
      mockTpsService,
      new PaymentService(testPaymentConnector, auditConnector, messagesApi),
      mibConnector,
      importView,
      exportView
    )

  private def amendHandler(): CheckYourAnswersAmendHandler =
    new CheckYourAnswersAmendHandler(
      actionBuilder,
      mockMibService,
      mockTpsService,
      new PaymentService(testPaymentConnector, auditConnector, messagesApi),
      amendImportView,
      amendExportView
    )

  private def controller(declarationJourney: DeclarationJourney): CheckYourAnswersController =
    new CheckYourAnswersController(
      controllerComponents,
      actionBuilder,
      newHandler(),
      amendHandler(),
      stubRepo(declarationJourney)
    )

  "onPageLoad" should {
    journeyTypes.foreach { journeyType =>
      s"redirect to /cannot-access-service for in-completed journeys for $journeyType" in {
        val sessionId                              = SessionId()
        val inCompletedJourney: DeclarationJourney =
          DeclarationJourney(aSessionId, Import, isAssistedDigital = false).copy(journeyType = journeyType)

        val request        = buildGet(routes.CheckYourAnswersController.onPageLoad.url, sessionId, inCompletedJourney)
        val eventualResult = controller(declarationJourney = inCompletedJourney).onPageLoad()(request)

        status(eventualResult) mustBe SEE_OTHER
        redirectLocation(eventualResult) mustBe Some(routes.CannotAccessPageController.onPageLoad.url)
      }
    }

    "return 200 for completed New journeys" in {
      val sessionId                   = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = New)
      givenADeclarationJourneyIsPersisted(journey)
      val request                     = buildGet(routes.CheckYourAnswersController.onPageLoad.url, sessionId, journey)

      when(mockMibService.paymentCalculations(any[Seq[ImportGoods]], any[GoodsDestination])(any[HeaderCarrier]))
        .thenReturn(Future.successful(CalculationResponse(aCalculationResults, WithinThreshold)))

      val result = controller(declarationJourney = journey).onPageLoad()(request)

      status(result) mustBe OK
    }

    "return 200 for completed Amend journeys" in {
      val sessionId                   = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = Amend)

      givenADeclarationJourneyIsPersisted(journey)
      givenPersistedDeclarationIsFound(
        declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult)),
        journey.declarationId
      )

      when(mockMibService.amendPlusOriginalCalculations(any[DeclarationJourney])(any[HeaderCarrier]))
        .thenReturn(
          OptionT.pure[Future](CalculationResponse(aTotalCalculationResult.calculationResults, WithinThreshold))
        )

      when(mockMibService.paymentCalculations(any[Seq[Goods]], any[GoodsDestination])(any[HeaderCarrier]))
        .thenReturn(Future.successful(CalculationResponse(aTotalCalculationResult.calculationResults, WithinThreshold)))

      val request = buildGet(routes.CheckYourAnswersController.onPageLoad.url, sessionId, journey)
      val result  = controller(declarationJourney = journey).onPageLoad()(request)

      status(result) mustBe OK
    }
  }

  "onSubmit" should {
    journeyTypes.foreach { journeyType =>
      s"redirect to /cannot-access-service for in-completed journeys for $journeyType" in {
        val sessionId                   = SessionId()
        val journey: DeclarationJourney =
          DeclarationJourney(aSessionId, Import, isAssistedDigital = false).copy(journeyType = journeyType)

        val request        = buildPost(routes.CheckYourAnswersController.onPageLoad.url, sessionId, journey)
        val eventualResult = controller(declarationJourney = journey).onSubmit()(request)

        status(eventualResult) mustBe SEE_OTHER
        redirectLocation(eventualResult) mustBe Some(routes.CannotAccessPageController.onPageLoad.url)
      }

      s"will invoke assisted digital on submit with $PayApiResponse if flag is set for $journeyType" in {
        val sessionId                                      = SessionId()
        val journey: DeclarationJourney                    =
          completedDeclarationJourney.copy(sessionId = sessionId, journeyType = journeyType, isAssistedDigital = true)
        val mockHandler: CheckYourAnswersNewHandler        = mock[CheckYourAnswersNewHandler]
        val mockAmendHandler: CheckYourAnswersAmendHandler = mock[CheckYourAnswersAmendHandler]

        def controller(declarationJourney: DeclarationJourney): CheckYourAnswersController =
          new CheckYourAnswersController(
            controllerComponents,
            actionBuilder,
            mockHandler,
            mockAmendHandler,
            stubRepo(journey)
          )

        givenTheUserIsAuthenticatedAndAuthorised()
        givenADeclarationJourneyIsPersisted(journey)
        givenDeclarationIsPersistedInBackend

        journeyType match {
          case New   =>
            when(mockHandler.onSubmitTps(any[Declaration])(any[RequestHeader], any[HeaderCarrier]))
              .thenReturn(Future.successful(Redirect("")))
          case Amend =>
            when(
              mockAmendHandler
                .onSubmitTps(any[DeclarationId], any[Amendment])(any[HeaderCarrier], any[Request[_]])
            )
              .thenReturn(Future.successful(Redirect("")))
        }

        val request =
          buildPost(
            routes.CheckYourAnswersController.onPageLoad.url,
            sessionId,
            journey,
            headers = Seq("authProviderId" -> "123")
          )

        val result: Future[Result] = controller(declarationJourney = journey).onSubmit()(request)

        status(result) mustBe SEE_OTHER
      }
    }

    "redirect to next page after successful form submit for New journeys" in {
      val sessionId                   = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = New)

      givenADeclarationJourneyIsPersisted(journey)
      givenDeclarationIsPersistedInBackend

      when(mockMibService.paymentCalculations(any[Seq[ImportGoods]], any[GoodsDestination])(any[HeaderCarrier]))
        .thenReturn(Future.successful(CalculationResponse(aCalculationResults, WithinThreshold)))

      val request = buildPost(routes.CheckYourAnswersController.onPageLoad.url, sessionId, journey)
      val result  = controller(declarationJourney = journey).onSubmit()(request)

      status(result) mustBe SEE_OTHER
    }

    "redirect to next page after successful form submit for Amend journeys" in {
      val sessionId                   = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = Amend)

      givenADeclarationJourneyIsPersisted(journey)
      val declarationWithResult: Declaration =
        declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult))

      when(mockMibService.findDeclaration(any[DeclarationId])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(declarationWithResult)))

      when(mockMibService.paymentCalculations(any[Seq[ImportGoods]], any[GoodsDestination])(any[HeaderCarrier]))
        .thenReturn(Future.successful(CalculationResponse(aTotalCalculationResult.calculationResults, WithinThreshold)))

      when(mockMibService.amendDeclaration(any[Declaration])(any[HeaderCarrier]))
        .thenReturn(Future.successful(declarationWithResult.declarationId))

      val request = buildPost(routes.CheckYourAnswersController.onPageLoad.url, sessionId, journey)

      val result: Future[Result] = controller(declarationJourney = journey).onSubmit()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("http://host")
    }
  }
}

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

import cats.data.OptionT
import org.scalamock.scalatest.MockFactory
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.RequestHeader
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.{MibConnector, PaymentConnector}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{JourneyId, PayApiRequest, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{payapi, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, URL}
import uk.gov.hmrc.merchandiseinbaggage.model.tpspayments.TpsId
import uk.gov.hmrc.merchandiseinbaggage.service.{MibService, PaymentService, TpsPaymentsService}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.{givenDeclarationIsPersistedInBackend, givenPersistedDeclarationIsFound}
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView, CheckYourAnswersExportView, CheckYourAnswersImportView}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.wiremock.MockStrideAuth._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Results._

class CheckYourAnswersControllerSpec extends DeclarationJourneyControllerSpec with MibConfiguration with WireMockSupport with MockFactory {

  private val httpClient = injector.instanceOf[HttpClient]
  private val importView = injector.instanceOf[CheckYourAnswersImportView]
  private val exportView = injector.instanceOf[CheckYourAnswersExportView]
  private val amendImportView = injector.instanceOf[CheckYourAnswersAmendImportView]
  private val amendExportView = injector.instanceOf[CheckYourAnswersAmendExportView]
  private val mibConnector = injector.instanceOf[MibConnector]
  private val auditConnector = injector.instanceOf[AuditConnector]
  private val mockMibService = mock[MibService]
  private val mockTpsService = mock[TpsPaymentsService]

  private lazy val testPaymentConnector = new PaymentConnector(httpClient, "") {
    override def sendPaymentRequest(requestBody: PayApiRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PayApiResponse] =
      Future.successful(payapi.PayApiResponse(JourneyId("5f3b"), URL("http://host")))
  }

  private def newHandler() =
    new CheckYourAnswersNewHandler(
      mockMibService,
      mockTpsService,
      new PaymentService(testPaymentConnector, auditConnector, messagesApi),
      mibConnector,
      importView,
      exportView,
    )

  private def amendHandler() =
    new CheckYourAnswersAmendHandler(
      actionBuilder,
      mockMibService,
      mockTpsService,
      new PaymentService(testPaymentConnector, auditConnector, messagesApi),
      amendImportView,
      amendExportView,
    )

  private def controller(declarationJourney: DeclarationJourney) =
    new CheckYourAnswersController(
      controllerComponents,
      actionBuilder,
      newHandler(),
      amendHandler(),
      stubRepo(declarationJourney)
    )

  "onPageLoad" should {
    journeyTypes.foreach { journeyType =>
      s"redirect to /cannot-access-service for in-completed journies for $journeyType" in {
        val sessionId = SessionId()
        val inCompletedJourney: DeclarationJourney = DeclarationJourney(aSessionId, Import).copy(journeyType = journeyType)

        val request = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)
        val eventualResult = controller(declarationJourney = inCompletedJourney).onPageLoad()(request)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.CannotAccessPageController.onPageLoad().url)
      }
    }

    s"return 200 for completed New journies" in {
      val sessionId = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = New)
      givenADeclarationJourneyIsPersisted(journey)
      val request = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

      (mockMibService
        .paymentCalculations(_: Seq[ImportGoods], _: GoodsDestination)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(CalculationResponse(aCalculationResults, WithinThreshold)))

      val eventualResult = controller(declarationJourney = journey).onPageLoad()(request)

      status(eventualResult) mustBe 200
    }

    s"return 200 for completed Amend journies" in {
      val sessionId = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = Amend)
      givenADeclarationJourneyIsPersisted(journey)
      givenPersistedDeclarationIsFound(declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult)), journey.declarationId)

      (mockMibService
        .amendPlusOriginalCalculations(_: DeclarationJourney)(_: HeaderCarrier))
        .expects(*, *)
        .returning(OptionT.pure[Future](CalculationResponse(aTotalCalculationResult.calculationResults, WithinThreshold)))

      (mockMibService
        .paymentCalculations(_: Seq[Goods], _: GoodsDestination)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(CalculationResponse(aTotalCalculationResult.calculationResults, WithinThreshold)))

      val request = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)
      val eventualResult = controller(declarationJourney = journey).onPageLoad()(request)

      status(eventualResult) mustBe 200
    }
  }

  "onSubmit" should {
    journeyTypes.foreach { journeyType =>
      s"redirect to /cannot-access-service for in-completed journies for $journeyType" in {
        val sessionId = SessionId()
        val journey: DeclarationJourney = DeclarationJourney(aSessionId, Import).copy(journeyType = journeyType)

        val request = buildPost(routes.CheckYourAnswersController.onPageLoad().url, sessionId)
        val eventualResult = controller(declarationJourney = journey).onSubmit()(request)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.CannotAccessPageController.onPageLoad().url)
      }
    }

    s"will invoke assisted digital on submit with $TpsId if flag is set" in new DeclarationJourneyControllerSpec {
      override def fakeApplication(): Application =
        new GuiceApplicationBuilder()
          .configure(
            Map(
              "microservice.services.auth.port" -> WireMockSupport.port,
              "assistedDigital"                 -> true
            ))
          .build()

      val sessionId = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = New)

      def controller(declarationJourney: DeclarationJourney) =
        new CheckYourAnswersController(
          controllerComponents,
          actionBuilder,
          mockHandler,
          amendHandler(),
          stubRepo(journey)
        )

      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(journey)
      givenDeclarationIsPersistedInBackend

      val mockHandler = mock[CheckYourAnswersNewHandler]

      (mockHandler
        .onSubmit(_: Declaration, _: String)(_: RequestHeader, _: HeaderCarrier))
        .expects(*, *, *, *)
        .returning(Future.successful(Redirect("")))
        .once()

      val request = buildPost(routes.CheckYourAnswersController.onPageLoad().url, sessionId)
        .withHeaders("authProviderId" -> "123")
      val eventualResult = controller(declarationJourney = journey).onSubmit()(request)

      status(eventualResult) mustBe 303
    }

    s"redirect to next page after successful form submit for New journies" in {
      val sessionId = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = New)
      givenADeclarationJourneyIsPersisted(journey)
      givenDeclarationIsPersistedInBackend

      (mockMibService
        .paymentCalculations(_: Seq[ImportGoods], _: GoodsDestination)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(CalculationResponse(aCalculationResults, WithinThreshold)))

      val request = buildPost(routes.CheckYourAnswersController.onPageLoad().url, sessionId)
      val eventualResult = controller(declarationJourney = journey).onSubmit()(request)

      status(eventualResult) mustBe 303
    }

    s"redirect to next page after successful form submit for Amend journies" in {
      val sessionId = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = Amend)

      givenADeclarationJourneyIsPersisted(journey)
      val declarationWithResult = declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult))

      (mockMibService
        .findDeclaration(_: DeclarationId)(_: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(Some(declarationWithResult)))

      (mockMibService
        .paymentCalculations(_: Seq[ImportGoods], _: GoodsDestination)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(CalculationResponse(aTotalCalculationResult.calculationResults, WithinThreshold)))

      (mockMibService
        .amendDeclaration(_: Declaration)(_: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(declarationWithResult.declarationId))

      val request = buildPost(routes.CheckYourAnswersController.onPageLoad().url, sessionId)
      val eventualResult = controller(declarationJourney = journey).onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some("http://host")
    }
  }
}

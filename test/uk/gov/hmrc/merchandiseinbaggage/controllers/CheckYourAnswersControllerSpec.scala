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
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, PaymentService}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.{givenDeclarationIsPersistedInBackend, givenPersistedDeclarationIsFound}
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView, CheckYourAnswersExportView, CheckYourAnswersImportView}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends DeclarationJourneyControllerSpec with MibConfiguration with WireMockSupport with MockFactory {

  private val httpClient = injector.instanceOf[HttpClient]
  private val importView = injector.instanceOf[CheckYourAnswersImportView]
  private val exportView = injector.instanceOf[CheckYourAnswersExportView]
  private val amendImportView = injector.instanceOf[CheckYourAnswersAmendImportView]
  private val amendExportView = injector.instanceOf[CheckYourAnswersAmendExportView]
  private val mibConnector = injector.instanceOf[MibConnector]
  private val mockCalculationService = mock[CalculationService]

  private lazy val testPaymentConnector = new PaymentConnector(httpClient, "") {
    override def sendPaymentRequest(requestBody: PayApiRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PayApiResponse] =
      Future.successful(payapi.PayApiResponse(JourneyId("5f3b"), URL("http://host")))
  }

  private def newHandler() =
    new CheckYourAnswersNewHandler(
      mockCalculationService,
      new PaymentService(testPaymentConnector),
      mibConnector,
      importView,
      exportView,
    )

  private def amendHandler() =
    new CheckYourAnswersAmendHandler(
      actionBuilder,
      mockCalculationService,
      new PaymentService(testPaymentConnector),
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

      (mockCalculationService
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

      (mockCalculationService
        .isAmendPlusOriginalOverThresholdImport(_: DeclarationJourney)(_: HeaderCarrier))
        .expects(*, *)
        .returning(OptionT.pure[Future](CalculationResponse(aTotalCalculationResult.calculationResults, WithinThreshold)))

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

    s"redirect to next page after successful form submit for New journies" in {
      val sessionId = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = New)
      givenADeclarationJourneyIsPersisted(journey)
      givenDeclarationIsPersistedInBackend

      (mockCalculationService
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

      (mockCalculationService
        .findDeclaration(_: DeclarationId)(_: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(Some(declarationWithResult)))

      (mockCalculationService
        .paymentCalculations(_: Seq[ImportGoods], _: GoodsDestination)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(CalculationResponse(aTotalCalculationResult.calculationResults, WithinThreshold)))

      (mockCalculationService
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

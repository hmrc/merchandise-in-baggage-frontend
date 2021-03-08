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
import uk.gov.hmrc.merchandiseinbaggage.connectors.{MibConnector, PaymentConnector}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResults
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{JourneyId, PayApiRequest, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{payapi, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, URL}
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, PaymentService}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.{givenDeclarationIsPersistedInBackend, givenPersistedDeclarationIsFound}
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

  private lazy val stubbedCalculation: CalculationResults => CalculationService = aPaymentCalculations =>
    new CalculationService(mibConnector) {
      override def paymentCalculations(importGoods: Seq[ImportGoods])(implicit hc: HeaderCarrier): Future[CalculationResults] =
        Future.successful(aPaymentCalculations)
  }

  private def newHandler(paymentCalcs: CalculationResults) =
    new CheckYourAnswersNewHandler(
      stubbedCalculation(paymentCalcs),
      new PaymentService(testPaymentConnector),
      mibConnector,
      importView,
      exportView,
    )

  private def amendHandler(paymentCalcs: CalculationResults) =
    new CheckYourAnswersAmendHandler(
      actionBuilder,
      new PaymentService(testPaymentConnector),
      stubbedCalculation(paymentCalcs),
      mibConnector,
      amendImportView,
      amendExportView,
    )

  private def controller(paymentCalcs: CalculationResults = aCalculationResults, declarationJourney: DeclarationJourney) =
    new CheckYourAnswersController(
      controllerComponents,
      actionBuilder,
      newHandler(paymentCalcs),
      amendHandler(paymentCalcs),
      stubRepo(declarationJourney)
    )

  val journeyTypes = List(New, Amend)

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
      val eventualResult = controller(declarationJourney = journey).onPageLoad()(request)

      status(eventualResult) mustBe 200
    }

    s"return 200 for completed Amend journies" in {
      val sessionId = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = Amend)
      givenADeclarationJourneyIsPersisted(journey)
      givenPersistedDeclarationIsFound(declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult)), journey.declarationId)

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

        val request = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)
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

      val request = buildPost(routes.CheckYourAnswersController.onPageLoad().url, sessionId)
      val eventualResult = controller(declarationJourney = journey).onSubmit()(request)

      status(eventualResult) mustBe 303
    }

    s"redirect to next page after successful form submit for Amend journies" in {
      val sessionId = SessionId()
      val journey: DeclarationJourney = completedDeclarationJourney.copy(sessionId = sessionId, journeyType = Amend)

      givenADeclarationJourneyIsPersisted(journey)
      givenPersistedDeclarationIsFound(declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult)), journey.declarationId)

      val request = buildPost(routes.CheckYourAnswersController.onPageLoad().url, sessionId)
      val eventualResult = controller(declarationJourney = journey).onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }
  }
}

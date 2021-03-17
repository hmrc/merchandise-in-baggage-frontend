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

import cats.data.OptionT
import com.softwaremill.quicklens._
import org.scalamock.scalatest.MockFactory
import play.api.mvc.Request
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResults
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationId, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, PaymentService}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.{givenDeclarationIsAmendedInBackend, givenPersistedDeclarationIsFound}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.utils.Utils.FutureOps
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersAmendHandlerSpec
    extends DeclarationJourneyControllerSpec with MibConfiguration with WireMockSupport with MockFactory {

  private val importView = injector.instanceOf[CheckYourAnswersAmendImportView]
  private val exportView = injector.instanceOf[CheckYourAnswersAmendExportView]
  private val mibConnector = injector.instanceOf[MibConnector]
  private val paymentService = mock[PaymentService]
  private val mockCalculationService = mock[CalculationService]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val stubbedCalculation: CalculationResults => CalculationService = aPaymentCalculations =>
    new CalculationService(mibConnector) {
      override def paymentCalculations(importGoods: Seq[ImportGoods])(implicit hc: HeaderCarrier): Future[CalculationResults] =
        Future.successful(aPaymentCalculations)
  }

  private def amendHandler(paymentCalcs: CalculationResults = aCalculationResults) =
    new CheckYourAnswersAmendHandler(
      actionBuilder,
      stubbedCalculation(paymentCalcs),
      mibConnector,
      paymentService,
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

        val eventualResult = amendHandler().onPageLoad(journey.copy(declarationType = importOrExport), amendment)

        status(eventualResult) mustBe OK
        contentAsString(eventualResult) must include(messageApi("checkYourAnswers.amend.title"))
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

        val handler = new CheckYourAnswersAmendHandler(
          actionBuilder,
          mockCalculationService,
          mibConnector,
          paymentService,
          importView,
          exportView,
        )

        val importOverThresholdGoods = aCalculationResults
          .modify(_.calculationResults.each)
          .setTo(aCalculationResult.modify(_.gbpAmount).setTo(AmountInPence(150000001)))

        (mockCalculationService
          .thresholdCheck(_: DeclarationJourney)(_: HeaderCarrier))
          .expects(journey, *)
          .returning(OptionT.pure(true))
          .once()

        if (importOrExport == Import) {
          (mockCalculationService
            .paymentCalculations(_: Seq[ImportGoods])(_: HeaderCarrier))
            .expects(completedAmendment(importOrExport).goods.importGoods, *)
            .returning(Future(importOverThresholdGoods))
            .once()
        }

        val amendment = completedAmendment(importOrExport)

        implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

        val eventualResult = handler.onPageLoad(journey.copy(declarationType = importOrExport), amendment)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.GoodsOverThresholdController.onPageLoad().url)
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

      givenPersistedDeclarationIsFound(declaration.copy(declarationId = id), id)
      givenADeclarationJourneyIsPersisted(importJourney)
      givenDeclarationIsAmendedInBackend

      (paymentService
        .sendPaymentRequest(_: MibReference, _: Option[Int], _: CalculationResults)(_: HeaderCarrier))
        .expects(*, *, *, *)
        .returning("http://callback".asFuture)

      val newAmendment = completedAmendment(declaration.declarationType)

      implicit val request: Request[_] = buildPost(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

      val eventualResult = amendHandler().onSubmit(id, newAmendment)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some("http://callback")
    }

    "will redirect to confirmation if totalTax is £0 and should not call pay api" in {
      val sessionId = SessionId()
      val id = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, createdAt = created, declarationId = id)

      givenPersistedDeclarationIsFound(declaration.copy(declarationId = id), id)
      givenADeclarationJourneyIsPersisted(importJourney)
      givenDeclarationIsAmendedInBackend

      (paymentService
        .sendPaymentRequest(_: MibReference, _: Option[Int], _: CalculationResults)(_: HeaderCarrier))
        .expects(*, *, *, *)
        .returning(routes.DeclarationConfirmationController.onPageLoad().url.asFuture)

      val newAmendment = completedAmendment(declaration.declarationType)

      implicit val request: Request[_] = buildPost(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

      val eventualResult = amendHandler(aCalculationResultsWithNoTax).onSubmit(id, newAmendment)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad().url)
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

      implicit val request: Request[_] = buildPost(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

      val eventualResult = amendHandler().onSubmit(stubbedId, newAmendment)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad().url)
    }
  }
}

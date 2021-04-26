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

import org.scalamock.scalatest.MockFactory
import play.api.mvc.Request
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, CalculationResults, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationId, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, PaymentService}
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
  private val paymentService = mock[PaymentService]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val stubbedCalculation: CalculationResults => CalculationService = aPaymentCalculations =>
    new CalculationService(mibConnector) {
      override def paymentCalculations(goods: Seq[Goods], destination: GoodsDestination)(
        implicit hc: HeaderCarrier): Future[CalculationResponse] =
        Future.successful(aCalculationResponse)
  }

  private def amendHandler(paymentCalcs: CalculationResults = aCalculationResults) =
    new CheckYourAnswersAmendHandler(
      actionBuilder,
      stubbedCalculation(paymentCalcs),
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

        implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

        val amendment = completedAmendment(importOrExport)

        val eventualResult = amendHandler().onPageLoad(journey, amendment)

        status(eventualResult) mustBe OK
        contentAsString(eventualResult) must include(messageApi("checkYourAnswers.amend.title"))
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

    implicit val request: Request[_] = buildPost(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

    val eventualResult = amendHandler().onSubmit(stubbedId, newAmendment)

    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad().url)
  }
}

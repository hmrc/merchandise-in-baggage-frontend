/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.service

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, reset, when}
import org.mockito.invocation.InvocationOnMock
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries, ThresholdAllowance}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.viewmodels.DeclarationView
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MibServiceSpec extends BaseSpecWithApplication with CoreTestData with OptionValues with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier  = HeaderCarrier()
  private val mockConnector: MibConnector = mock(classOf[MibConnector])
  private val service: MibService         = new MibService(mockConnector)

  private val (gbpAmount, duty, vat)           = (7835, 0, 1567)
  private val stubbedResult: CalculationResult =
    CalculationResult(
      aGoods,
      AmountInPence(gbpAmount),
      AmountInPence(duty),
      AmountInPence(vat),
      Some(aConversionRatePeriod)
    )

  override def beforeEach(): Unit =
    reset(mockConnector)

  "retrieve payment calculations from mib backend" in {

    val expected: CalculationResponse = CalculationResponse(CalculationResults(List(stubbedResult)), WithinThreshold)

    val calculationRequests: Seq[CalculationRequest] =
      expected.results.calculationResults.map(_.goods.calculationRequest(GreatBritain))

    when(mockConnector.calculatePayments(eqTo(calculationRequests))(any[HeaderCarrier]))
      .thenReturn(Future.successful(CalculationResponse(CalculationResults(Seq(stubbedResult)), WithinThreshold)))

    service.paymentCalculations(Seq(aGoods), GreatBritain).futureValue mustBe expected
  }

  "check if over threshold for amend journey" in { // not right
    val amended: DeclarationJourney = completedImportJourneyWithGoodsOverThreshold
      .copy(journeyType = Amend)

    when(mockConnector.calculatePaymentsAmendPlusExisting(any[CalculationAmendRequest])(any[HeaderCarrier]))
      .thenReturn(Future.successful(CalculationResponse(CalculationResults(Seq(stubbedResult)), OverThreshold)))

    service.amendPlusOriginalCalculations(amended).value.futureValue.get.thresholdCheck mustBe OverThreshold
  }

  "check threshold allowance" in {
    val entries                                  = completedGoodsEntries(Import)
    val calculationResponse: CalculationResponse =
      CalculationResponse(CalculationResults(Seq(stubbedResult)), WithinThreshold)

    when(mockConnector.calculatePayments(any[Seq[CalculationRequest]])(any[HeaderCarrier]))
      .thenReturn(Future.successful(calculationResponse))

    val actual: Option[ThresholdAllowance] =
      service.thresholdAllowance(Some(GreatBritain), entries, New, aDeclarationId).value.futureValue
    actual mustBe Some(
      ThresholdAllowance(
        DeclarationGoods(List(aImportGoods)),
        DeclarationGoods(List(aImportGoods)),
        calculationResponse,
        GreatBritain
      )
    )
  }

  "check threshold allowance including existing declaration for amends" in {
    val entries: GoodsEntries = completedGoodsEntries(Import)
    val declarationId         = aDeclarationId
    val existingDeclaration   =
      declaration.copy(amendments = declaration.amendments.map(_.copy(paymentStatus = Some(Paid))))

    val calculationResponse: CalculationResponse =
      CalculationResponse(CalculationResults(Seq(stubbedResult)), WithinThreshold)

    when(mockConnector.findDeclaration(any[DeclarationId])(any[HeaderCarrier]))
      .thenReturn(Future.successful(Some(existingDeclaration)))

    when(mockConnector.calculatePayments(any[Seq[CalculationRequest]])(any[HeaderCarrier]))
      .thenReturn(Future.successful(calculationResponse))

    val actual: Option[ThresholdAllowance] =
      service.thresholdAllowance(Some(GreatBritain), entries, Amend, declarationId).value.futureValue
    val allGoods: Seq[Goods]               =
      existingDeclaration.declarationGoods.goods ++ entries.declarationGoodsIfComplete.get.goods

    actual.value.currentGoods mustBe entries.declarationGoodsIfComplete.value
    actual.value.allGoods.goods must contain theSameElementsAs allGoods

    actual.value.calculationResponse mustBe calculationResponse
  }

  s"add only goods in $Paid or $NotRequired status" in {
    val declarationId   = aDeclarationId
    val unknown         = completedAmendment(Import).copy(paymentStatus = None)
    val expectedGoods   = Seq(aImportGoods)
    val plusUnsetStatus = declaration.copy(
      declarationId = declarationId,
      amendments = declaration.amendments ++ Seq(unknown)
    )

    when(mockConnector.findDeclaration(any[DeclarationId])(any[HeaderCarrier]))
      .thenReturn(Future.successful(Some(plusUnsetStatus)))

    service.addGoods(Amend, declarationId, expectedGoods).value.futureValue mustBe Some(
      expectedGoods ++ DeclarationView.allGoods(plusUnsetStatus)
    )
  }

  "send a request for calculation including declared goods plus amendments goods" in {
    val amendments: Seq[Amendment]     = aAmendment :: aAmendmentPaid :: aAmendmentNotRequired :: Nil
    val foundDeclaration: Declaration  = declaration.copy(amendments = amendments)
    val expectedTotalGoods: Seq[Goods] =
      foundDeclaration.declarationGoods.goods ++ aAmendmentPaid.goods.goods ++ aAmendmentNotRequired.goods.goods

    when(mockConnector.calculatePayments(any[Seq[CalculationRequest]])(any[HeaderCarrier]))
      .thenAnswer { (invocation: InvocationOnMock) =>
        val calculationRequests: Seq[CalculationRequest] =
          invocation.getArguments.head.asInstanceOf[Seq[CalculationRequest]]
        if (calculationRequests.map(_.goods) == expectedTotalGoods) {
          Future.successful(aCalculationResponse)
        } else {
          Future.failed(new Exception("Arguments not matching"))
        }
      }

    val actual: ThresholdAllowance = service.thresholdAllowance(foundDeclaration).futureValue
    actual mustBe a[ThresholdAllowance]
  }

  "send a request for calculation including declared goods plus amendments goods for export" in {
    val amendments: Seq[Amendment]     = aAmendment :: aAmendmentPaid :: aAmendmentNotRequired :: Nil
    val foundDeclaration: Declaration  = declaration
      .copy(
        declarationType = Export,
        amendments = amendments
      )
    val expectedTotalGoods: Seq[Goods] = foundDeclaration.declarationGoods.goods ++ amendments.flatMap(_.goods.goods)

    when(mockConnector.calculatePayments(any[Seq[CalculationRequest]])(any[HeaderCarrier]))
      .thenAnswer { (invocation: InvocationOnMock) =>
        val calculationRequests: Seq[CalculationRequest] =
          invocation.getArguments.head.asInstanceOf[Seq[CalculationRequest]]
        if (calculationRequests.map(_.goods).size == expectedTotalGoods.size) {
          Future.successful(aCalculationResponse)
        } else {
          Future.failed(new Exception("Arguments not matching"))
        }
      }

    val actual: ThresholdAllowance = service.thresholdAllowance(foundDeclaration).futureValue
    actual mustBe a[ThresholdAllowance]
  }
}

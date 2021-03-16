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

package uk.gov.hmrc.merchandiseinbaggage.service

import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationRequest, CalculationResult, CalculationResults}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalculationServiceSpec extends BaseSpecWithApplication with WireMockSupport with CoreTestData with MockFactory {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val mockConnector = mock[MibConnector]
  private val service = new CalculationService(mockConnector)

  "retrieve payment calculations from mib backend" in {
    val stubbedResult =
      CalculationResult(aImportGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val expected = CalculationResults(List(stubbedResult))

    (mockConnector
      .calculatePayments(_: Seq[CalculationRequest])(_: HeaderCarrier))
      .expects(expected.calculationResults.map(_.goods.calculationRequest), *)
      .returning(Future.successful(Seq(stubbedResult)))

    service.paymentCalculations(Seq(aImportGoods)).futureValue mustBe expected
  }

  "check if over threshold for amend journey" in {
    val stubbedResult =
      CalculationResult(aImportGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val expected = CalculationResults(List(stubbedResult))
    val amended = completedImportJourneyWithGoodsOverThreshold
      .copy(journeyType = Amend)

    val declaration = amended.toDeclaration
      .copy(
        maybeTotalCalculationResult =
          Some(TotalCalculationResult(expected, expected.totalGbpValue, expected.totalTaxDue, expected.totalDutyDue, expected.totalVatDue)))

    (mockConnector
      .calculatePayments(_: Seq[CalculationRequest])(_: HeaderCarrier))
      .expects(declaration.declarationGoods.goods.map { case i: ImportGoods => i }.map(_.calculationRequest), *)
      .returning(Future.successful(Seq(stubbedResult)))

    (mockConnector
      .findDeclaration(_: DeclarationId)(_: HeaderCarrier))
      .expects(amended.declarationId, *)
      .returning(Future.successful(Some(declaration)))

    service.thresholdCheck(amended).value.futureValue mustBe Some(false)
  }

  "returns true if over threshold for amend journey" in {
    val stubbedResult =
      CalculationResult(aImportGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val expected = CalculationResults(List(stubbedResult))
    val amended = completedImportJourneyWithGoodsOverThreshold
      .copy(journeyType = Amend)

    val declaration = amended.toDeclaration
      .copy(
        maybeTotalCalculationResult =
          Some(TotalCalculationResult(expected, expected.totalGbpValue, expected.totalTaxDue, expected.totalDutyDue, expected.totalVatDue)))

    (mockConnector
      .calculatePayments(_: Seq[CalculationRequest])(_: HeaderCarrier))
      .expects(declaration.declarationGoods.goods.map { case i: ImportGoods => i }.map(_.calculationRequest), *)
      .returning(Future.successful(Seq(stubbedResult.copy(gbpAmount = AmountInPence(150000)))))

    (mockConnector
      .findDeclaration(_: DeclarationId)(_: HeaderCarrier))
      .expects(amended.declarationId, *)
      .returning(Future.successful(Some(declaration)))

    service.thresholdCheck(amended).value.futureValue mustBe Some(true)
  }

  "return false for any journey that is NOT amend" in {
    service.thresholdCheck(startedImportToGreatBritainJourney.copy(journeyType = Amend)).value.futureValue mustBe Some(false)
  }
}

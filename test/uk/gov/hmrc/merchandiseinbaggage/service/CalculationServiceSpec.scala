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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation._
import uk.gov.hmrc.merchandiseinbaggage.model.core.ThresholdAllowance
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
      CalculationResult(aGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val expected = CalculationResponse(CalculationResults(List(stubbedResult)), WithinThreshold)

    (mockConnector
      .calculatePayments(_: Seq[CalculationRequest])(_: HeaderCarrier))
      .expects(expected.results.calculationResults.map(_.goods.calculationRequest(GreatBritain)), *)
      .returning(Future.successful(CalculationResponse(CalculationResults(Seq(stubbedResult)), WithinThreshold)))

    service.paymentCalculations(Seq(aGoods), GreatBritain).futureValue mustBe expected
  }

  "check if over threshold for amend journey" in {
    val stubbedResult =
      CalculationResult(aGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val amended = completedImportJourneyWithGoodsOverThreshold
      .copy(journeyType = Amend)

    (mockConnector
      .calculatePaymentsAmendPlusExisting(_: CalculationAmendRequest)(_: HeaderCarrier))
      .expects(*, *)
      .returning(Future.successful(CalculationResponse(CalculationResults(Seq(stubbedResult)), WithinThreshold)))

    service.amendPlusOriginalCalculations(amended).value.futureValue.get.thresholdCheck mustBe WithinThreshold
  }

  "check threshold allowance" in {
    val entries = completedGoodsEntries(Import)
    val stubbedResult =
      CalculationResult(aImportGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val calculationResponse = CalculationResponse(CalculationResults(Seq(stubbedResult)), WithinThreshold)

    (mockConnector
      .calculatePayments(_: Seq[CalculationRequest])(_: HeaderCarrier))
      .expects(*, *)
      .returning(Future.successful(calculationResponse))

    val actual = service.thresholdAllowance(Some(GreatBritain), entries).value.futureValue
    actual mustBe Some(ThresholdAllowance(DeclarationGoods(List(aImportGoods)), calculationResponse, GreatBritain))
  }
}

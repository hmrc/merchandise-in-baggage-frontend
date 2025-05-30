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

package uk.gov.hmrc.merchandiseinbaggage.model.core

import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResult, CalculationResults}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, YesNoDontKnow}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class CalculationResultsSpec extends BaseSpec with CoreTestData {

  "Calculate each total tax due" in {
    val calculationResult: CalculationResult =
      CalculationResult(aGoods, AmountInPence(20L), AmountInPence(10), AmountInPence(30), None)
    val calculations                         = CalculationResults(Seq(calculationResult))

    calculations.totalTaxDue mustBe AmountInPence(10 + 30)
    calculations.totalDutyDue mustBe AmountInPence(10)
    calculations.totalVatDue mustBe AmountInPence(30)
  }

  "Check if proof of origin needed" in {
    val calculationResultEU: CalculationResult =
      CalculationResult(aGoods, AmountInPence(100001), AmountInPence(10), AmountInPence(30), None)

    val calculationResultUSA: CalculationResult =
      CalculationResult(
        aGoods.copy(producedInEu = YesNoDontKnow.No),
        AmountInPence(20L),
        AmountInPence(10),
        AmountInPence(30),
        None
      )

    val calculations = CalculationResults(Seq(calculationResultEU, calculationResultUSA))

    val firstResult = calculations.calculationResults.head.copy(gbpAmount = AmountInPence(100000))

    calculations.proofOfOriginNeeded mustBe true
    calculations
      .copy(calculationResults = firstResult +: calculations.calculationResults.tail)
      .proofOfOriginNeeded mustBe false
  }
}

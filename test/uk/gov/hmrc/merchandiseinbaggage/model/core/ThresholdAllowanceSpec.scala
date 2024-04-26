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

package uk.gov.hmrc.merchandiseinbaggage.model.core

import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResults
import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, DeclarationGoods}
import uk.gov.hmrc.merchandiseinbaggage.model.core.ThresholdAllowance._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class ThresholdAllowanceSpec extends BaseSpec with CoreTestData {

  "return threshold allowance left for import" in {
    val allowanceGbpAmountOne =
      aThresholdAllowance.calculationResponse.results.calculationResults.map(_.copy(gbpAmount = AmountInPence(7179)))
    val allowanceGbpAmountTwo =
      aThresholdAllowance.calculationResponse.results.calculationResults.map(_.copy(gbpAmount = AmountInPence(7180)))

    val allowanceOne =
      aThresholdAllowance.copy(
        calculationResponse = aThresholdAllowance.calculationResponse.copy(
          results = CalculationResults(allowanceGbpAmountOne)
        )
      )

    val allowanceTwo =
      aThresholdAllowance.copy(
        calculationResponse = aThresholdAllowance.calculationResponse.copy(
          results = CalculationResults(allowanceGbpAmountTwo)
        )
      )

    allowanceOne.allowanceLeft mustBe 2428.21
    allowanceTwo.allowanceLeft mustBe 2428.20
  }

  "return threshold allowance left for export" in {
    val goodOne   = aExportGoods.copy(purchaseDetails = aExportGoods.purchaseDetails.copy(amount = "71.75"))
    val goodTwo   = aExportGoods.copy(purchaseDetails = aExportGoods.purchaseDetails.copy(amount = "70.00"))
    val allowance = aThresholdAllowance.copy(allGoods = DeclarationGoods(Seq(goodOne, goodTwo)))

    allowance.allowanceLeft mustBe 2358.25
  }

  "format correctly" in {
    formatter.format(1428.20) mustBe "1,428.20"
    formatter.format(100000428.200) mustBe "100,000,428.20"

    aThresholdAllowance.toUIString mustBe "£2,499.90"
  }
}

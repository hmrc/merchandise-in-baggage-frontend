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

package uk.gov.hmrc.merchandiseinbaggage.model.core

import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}
import uk.gov.hmrc.merchandiseinbaggage.model.core.ThresholdAllowance._
import com.softwaremill.quicklens._

class ThresholdAllowanceSpec extends BaseSpec with CoreTestData {

  "return threshold allowance left for import" in {
    val allowance = aThresholdAllowance.modify(_.calculationResponse.results.calculationResults.each.gbpAmount.value).setTo(7179)
    val allowanceTwo = aThresholdAllowance.modify(_.calculationResponse.results.calculationResults.each.gbpAmount.value).setTo(7180)
    allowance.allowanceLeft mustBe 1428.21
    allowanceTwo.allowanceLeft mustBe 1428.20
  }

  "return threshold allowance left for export" in {
    val goodOne = aExportGoods.modify(_.purchaseDetails.amount).setTo("71.75")
    val goodTwo = aExportGoods.modify(_.purchaseDetails.amount).setTo("70.00")
    val allowance = aThresholdAllowance
      .modify(_.goods.goods)
      .setTo(Seq(goodOne, goodTwo))

    allowance.allowanceLeft mustBe 1358.25
  }

  "format correctly" in {
    formatter.format(1428.20) mustBe "1,428.20"
    formatter.format(100000428.200) mustBe "100,000,428.20"

    aThresholdAllowance.toUIString mustBe "£1,499.90"
  }
}

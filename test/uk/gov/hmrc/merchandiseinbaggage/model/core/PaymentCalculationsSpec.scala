/*
 * Copyright 2020 HM Revenue & Customs
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

import uk.gov.hmrc.merchandiseinbaggage.model.calculation.CalculationResult
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class PaymentCalculationsSpec extends BaseSpec with CoreTestData {

  "Calculate each total tax due" in {
    val calculationResultTwo: CalculationResult = CalculationResult(AmountInPence(20L), AmountInPence(10), AmountInPence(30))
    val calculations = PaymentCalculations(Seq(aPaymentCalculation, PaymentCalculation(aGoods, calculationResultTwo)))

    calculations.totalTaxDue mustBe AmountInPence(10 + 5 + 30 + 7)
    calculations.totalDutyDue mustBe AmountInPence(10 + 5)
    calculations.totalVatDue mustBe AmountInPence(30 + 7)
  }
}

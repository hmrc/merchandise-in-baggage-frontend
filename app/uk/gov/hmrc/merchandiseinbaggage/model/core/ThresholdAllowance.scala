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

import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResponse
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationGoods, GoodsDestination}

import scala.util.Try

case class ThresholdAllowance(goods: DeclarationGoods, calculationResponse: CalculationResponse, destination: GoodsDestination)

object ThresholdAllowance {

  val formatter = "%,.2f"

  implicit class ThresholdAllowanceLeft(allowance: ThresholdAllowance) {
    import allowance._
    def allowanceLeft: Double =
      Try {
        val sum = calculationResponse.results.calculationResults.map(_.gbpAmount.value).sum
        (destination.threshold.value.toDouble - sum.toDouble) / 100
      }.getOrElse((destination.threshold.value - calculationResponse.results.calculationResults.map(_.gbpAmount.value).sum) / 100)
  }
}

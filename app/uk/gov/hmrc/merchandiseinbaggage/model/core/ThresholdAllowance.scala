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

import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, ThresholdCheck}
import uk.gov.hmrc.merchandiseinbaggage.model.api.*
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

import scala.util.Try

case class ThresholdAllowance(
  currentGoods: DeclarationGoods,
  allGoods: DeclarationGoods,
  calculationResponse: CalculationResponse,
  destination: GoodsDestination
) {
  val currentStatus: ThresholdCheck = calculationResponse.thresholdCheck
}

object ThresholdAllowance {

  private[core] val formatter = "%,.2f"

  implicit class ThresholdAllowanceLeft(allowance: ThresholdAllowance) {
    import allowance.*
    def allowanceLeft: Double =
      allGoods.goods.headOption
        .map { g =>
          calculateAllowanceLeft(allGoods, calculationResponse, destination, g)
        }
        .getOrElse(0)

    def toUIString: String = s"£${formatter.format(allowanceLeft)}"
  }

  private def calculateAllowanceLeft(
    goods: DeclarationGoods,
    calculationResponse: CalculationResponse,
    destination: GoodsDestination,
    good: Goods
  ): Double =
    Try {
      good match {
        case _: ImportGoods =>
          val sum = calculationResponse.results.calculationResults.map(_.gbpAmount.value).sum
          (destination.threshold.value.toDouble - sum.toDouble) / 100
        case _: ExportGoods =>
          val sum = goods.goods.map(_.purchaseDetails.numericAmount.toDouble).sum
          destination.threshold.inPounds.toDouble - sum
      }
    }.getOrElse(0.0)
}

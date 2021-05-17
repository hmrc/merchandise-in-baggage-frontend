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
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationGoods, ExportGoods, Goods, GoodsDestination, ImportGoods}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

import scala.util.Try

case class ThresholdAllowance(goods: DeclarationGoods, calculationResponse: CalculationResponse, destination: GoodsDestination)

object ThresholdAllowance {

  private[core] val formatter = "%,.2f"

  //TODO this should be all done in BE
  implicit class ThresholdAllowanceLeft(allowance: ThresholdAllowance) {
    import allowance._
    def allowanceLeft: Double =
      goods.goods.headOption
        .map { g =>
          calculateAllowanceLeft(goods, calculationResponse, destination, g)
        }
        .getOrElse(0)

    def toUIString: String = s"£${formatter.format(allowanceLeft)}"
  }

  private def calculateAllowanceLeft(
    goods: DeclarationGoods,
    calculationResponse: CalculationResponse,
    destination: GoodsDestination,
    good: Goods): Double =
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

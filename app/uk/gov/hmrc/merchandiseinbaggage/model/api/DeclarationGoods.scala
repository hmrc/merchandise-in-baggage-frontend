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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.Country
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationRequest
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

case class Goods(
  categoryQuantityOfGoods: CategoryQuantityOfGoods,
  goodsVatRate: GoodsVatRate,
  countryOfPurchase: Country,
  purchaseDetails: PurchaseDetails) {

  val calculationRequest: CalculationRequest =
    CalculationRequest(purchaseDetails.numericAmount, purchaseDetails.currency, countryOfPurchase, goodsVatRate)
}

object Goods {
  implicit val format: OFormat[Goods] = Json.format[Goods]
}

case class DeclarationGoods(goods: Seq[Goods])

object DeclarationGoods {
  implicit val format: OFormat[DeclarationGoods] = Json.format[DeclarationGoods]

  def apply(goods: Goods): DeclarationGoods = DeclarationGoods(Seq(goods))
}

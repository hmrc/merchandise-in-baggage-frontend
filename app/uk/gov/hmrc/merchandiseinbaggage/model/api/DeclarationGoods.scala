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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.*

sealed trait Goods {
  val category: String
  val purchaseDetails: PurchaseDetails
}

final case class ImportGoods(
  category: String,
  goodsVatRate: GoodsVatRate,
  producedInEu: YesNoDontKnow,
  purchaseDetails: PurchaseDetails
) extends Goods

object ImportGoods {
  implicit val format: OFormat[ImportGoods] = Json.format[ImportGoods]
}

final case class ExportGoods(
  category: String,
  destination: Country,
  purchaseDetails: PurchaseDetails
) extends Goods

object ExportGoods {
  implicit val format: OFormat[ExportGoods] = Json.format[ExportGoods]
}

object Goods {
  implicit val writes: Writes[Goods] = Writes[Goods] {
    case ig: ImportGoods => ImportGoods.format.writes(ig)
    case eg: ExportGoods => ExportGoods.format.writes(eg)
  }

  // TODO: Check catch all case
  implicit val reads: Reads[Goods] = Reads[Goods] {
    case json: JsObject if json.keys.contains("producedInEu") =>
      JsSuccess(json.as[ImportGoods])
    case json: JsObject if json.keys.contains("destination")  =>
      JsSuccess(json.as[ExportGoods])
    case _                                                    => JsError()
  }
}

case class DeclarationGoods(goods: Seq[Goods])

object DeclarationGoods {
  implicit val format: OFormat[DeclarationGoods] = Json.format[DeclarationGoods]
}

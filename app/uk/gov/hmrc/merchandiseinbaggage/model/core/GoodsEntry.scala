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

import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.*
import uk.gov.hmrc.merchandiseinbaggage.model.api.*

sealed trait GoodsEntry {
  val maybeCategory: Option[String]
  val maybePurchaseDetails: Option[PurchaseDetails]

  def goodsIfComplete: Option[Goods]

  def isComplete: Boolean = goodsIfComplete.isDefined
}

final case class ImportGoodsEntry(
  override val maybeCategory: Option[String] = None,
  maybeGoodsVatRate: Option[GoodsVatRate] = None,
  maybeProducedInEu: Option[YesNoDontKnow] = None,
  override val maybePurchaseDetails: Option[PurchaseDetails] = None
) extends GoodsEntry {
  override def goodsIfComplete: Option[ImportGoods] =
    for {
      category        <- maybeCategory
      goodsVatRate    <- maybeGoodsVatRate
      producedInEu    <- maybeProducedInEu
      purchaseDetails <- maybePurchaseDetails
    } yield ImportGoods(category, goodsVatRate, producedInEu, purchaseDetails)
}

object ImportGoodsEntry {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(optionHandlers = OptionHandlers.WritesNull)

  implicit val format: OFormat[ImportGoodsEntry] = Json.format[ImportGoodsEntry]

  val empty: ImportGoodsEntry = ImportGoodsEntry()
}

final case class ExportGoodsEntry(
  override val maybeCategory: Option[String] = None,
  maybeDestination: Option[Country] = None,
  override val maybePurchaseDetails: Option[PurchaseDetails] = None
) extends GoodsEntry {
  override def goodsIfComplete: Option[ExportGoods] =
    for {
      category        <- maybeCategory
      destination     <- maybeDestination
      purchaseDetails <- maybePurchaseDetails
    } yield ExportGoods(category, destination, purchaseDetails)
}

object ExportGoodsEntry {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(optionHandlers = OptionHandlers.WritesNull)

  implicit val format: OFormat[ExportGoodsEntry] = Json.format[ExportGoodsEntry]

  val empty: ExportGoodsEntry = ExportGoodsEntry()
}

object GoodsEntry {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(optionHandlers = OptionHandlers.WritesNull)

  implicit val writes: Writes[GoodsEntry] = Writes[GoodsEntry] {
    case ig: ImportGoodsEntry => ImportGoodsEntry.format.writes(ig)
    case eg: ExportGoodsEntry => ExportGoodsEntry.format.writes(eg)
  }

  // TODO: Check catch all case
  implicit val reads: Reads[GoodsEntry] = Reads[GoodsEntry] {
    case json: JsObject if json.keys.contains("maybeProducedInEu") =>
      JsSuccess(json.as[ImportGoodsEntry])
    case json: JsObject if json.keys.contains("maybeDestination")  =>
      JsSuccess(json.as[ExportGoodsEntry])
    case _                                                         => JsError()
  }
}

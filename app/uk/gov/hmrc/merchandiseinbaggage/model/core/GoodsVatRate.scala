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

import enumeratum.EnumEntry
import play.api.libs.json.Format
import uk.gov.hmrc.merchandiseinbaggage.model.{Enum, EnumEntryRadioItemSupport, EnumFormat, RadioSupport}

import scala.collection.immutable

sealed trait GoodsVatRate extends EnumEntry with EnumEntryRadioItemSupport {
  val value: Int
}

object GoodsVatRate {
  implicit val format: Format[GoodsVatRate] = EnumFormat(GoodsVatRates)
}

object GoodsVatRates extends Enum[GoodsVatRate] with RadioSupport[GoodsVatRate] {
  override val baseMessageKey: String = "goodsVatRate"

  override val values: immutable.IndexedSeq[GoodsVatRate] = findValues

  case object Zero extends GoodsVatRate { override val value: Int = 0 }
  case object Five extends GoodsVatRate { override val value: Int = 5 }
  case object Twenty extends GoodsVatRate { override val value: Int = 20 }
}

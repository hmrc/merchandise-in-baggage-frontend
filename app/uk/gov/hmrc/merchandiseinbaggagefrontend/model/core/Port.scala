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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.core

import enumeratum.EnumEntry
import play.api.libs.json._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.{EnumFormat, Enum}

import scala.collection.immutable

object Port {
  implicit val format: Format[Port] = EnumFormat(Ports)
}

sealed trait Port extends EnumEntry {
  val rollOnRollOff: Boolean = false
  val display: String = entryName
}

sealed abstract class RollOnRollOffPort extends Port {
  override val rollOnRollOff: Boolean = true
}

object Ports extends Enum[Port] {
  override val baseMessageKey: String = "placeOfArrival"
  override val values: immutable.IndexedSeq[Port] = findValues

  case object Dover extends RollOnRollOffPort

  case object Heathrow extends Port

}

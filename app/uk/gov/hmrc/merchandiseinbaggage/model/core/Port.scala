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
import play.api.libs.json._
import uk.gov.hmrc.merchandiseinbaggage.model.core.Ports.{footPassengerOnlyPorts, vehiclePorts}
import uk.gov.hmrc.merchandiseinbaggage.model.{Enum, EnumFormat}

import scala.collection.immutable

sealed trait Port extends EnumEntry {
  val vehiclePort: Boolean
  val display: String = entryName
}

object Port {
  implicit val format: Format[Port] = EnumFormat(Ports)
}

sealed trait VehiclePort extends Port {
  override val vehiclePort: Boolean = true
}

sealed trait FootPassengerOnlyPort extends Port {
  override val vehiclePort: Boolean = false
}

object Ports extends Enum[Port] {
  override val baseMessageKey: String = "placeOfArrival"
  override val values: immutable.IndexedSeq[Port] = findValues

  val vehiclePorts: Map[String, VehiclePort] =
    values.flatMap {
      case port: VehiclePort => Some(port.entryName -> port)
      case _ => None
    }.toMap

  val footPassengerOnlyPorts: Map[String, FootPassengerOnlyPort] =
    values.flatMap {
      case port: FootPassengerOnlyPort => Some(port.entryName -> port)
      case _ => None
    }.toMap

  case object Dover extends VehiclePort

  case object Heathrow extends FootPassengerOnlyPort

}

private case class PortSubTypeFormatter[P <: Port](ports: Map[String, P], label: String) {
  val format: Format[P] = Format(
    Reads {
      case JsString(value) => ports.get(value).map(JsSuccess(_)).getOrElse(JsError(s"Unknown $label value: $value"))
      case _ => JsError("Can only parse String")
    },
    Writes(port => JsString(port.entryName)))
}

object VehiclePort {
  implicit val format: Format[VehiclePort] = PortSubTypeFormatter(vehiclePorts, "VehiclePort").format
}

object FootPassengerOnlyPort {
  implicit val format: Format[FootPassengerOnlyPort] = PortSubTypeFormatter(footPassengerOnlyPorts, "FootPassengerOnlyPort").format
}


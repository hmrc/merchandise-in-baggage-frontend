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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{FootPassengerOnlyPort, Port, VehiclePort, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo.{No, Yes}

sealed trait JourneyDetails {
  val placeOfArrival: Port
  val dateOfArrival: LocalDate
  val formattedDateOfArrival: String = DateTimeFormatter.ofPattern("dd MMM yyyy").format(dateOfArrival)
  val travellingByVehicle: YesNo = No
  val maybeRegistrationNumber: Option[String] = None
}

case class JourneyViaFootPassengerOnlyPort(placeOfArrival: FootPassengerOnlyPort, dateOfArrival: LocalDate) extends JourneyDetails

case class JourneyOnFootViaVehiclePort(placeOfArrival: VehiclePort, dateOfArrival: LocalDate) extends JourneyDetails

case class JourneyInSmallVehicle(placeOfArrival: VehiclePort, dateOfArrival: LocalDate, registrationNumber: String) extends JourneyDetails {
  override val travellingByVehicle: YesNo = Yes
  override val maybeRegistrationNumber: Option[String] = Some(registrationNumber)
}

object JourneyDetails {
  implicit val format: OFormat[JourneyDetails] = Json.format[JourneyDetails]
}

object JourneyViaFootPassengerOnlyPort {
  implicit val format: OFormat[JourneyViaFootPassengerOnlyPort] = Json.format[JourneyViaFootPassengerOnlyPort]
}

object JourneyOnFootViaVehiclePort {
  implicit val format: OFormat[JourneyOnFootViaVehiclePort] = Json.format[JourneyOnFootViaVehiclePort]
}

object JourneyInSmallVehicle {
  implicit val format: OFormat[JourneyInSmallVehicle] = Json.format[JourneyInSmallVehicle]
}

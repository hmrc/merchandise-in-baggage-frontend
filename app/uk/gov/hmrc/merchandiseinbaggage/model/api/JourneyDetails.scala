/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json._
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}

sealed trait JourneyDetails {
  val port: Port
  val dateOfTravel: LocalDate
  val travellingByVehicle: YesNo              = No
  val maybeRegistrationNumber: Option[String] = None
}

case class JourneyOnFoot(port: Port, dateOfTravel: LocalDate) extends JourneyDetails

case class JourneyInSmallVehicle(port: Port, dateOfTravel: LocalDate, registrationNumber: String)
    extends JourneyDetails {
  override val travellingByVehicle: YesNo              = Yes
  override val maybeRegistrationNumber: Option[String] = Some(registrationNumber)
}

object JourneyDetails {
  implicit val format: OFormat[JourneyDetails] = new OFormat[JourneyDetails] {
    override def reads(json: JsValue): JsResult[JourneyDetails] = {
      val port         = (json \ "port").as[Port]
      val dateOfTravel = (json \ "dateOfTravel").as[LocalDate]

      (json \ "registrationNumber").asOpt[String] match {
        case Some(regNo) => JsSuccess(JourneyInSmallVehicle(port, dateOfTravel, regNo))
        case None        => JsSuccess(JourneyOnFoot(port, dateOfTravel))
      }
    }

    override def writes(o: JourneyDetails): JsObject =
      o match {
        case p: JourneyOnFoot         => JourneyOnFoot.format.writes(p)
        case p: JourneyInSmallVehicle => JourneyInSmallVehicle.format.writes(p)
      }
  }
}

object JourneyOnFoot {
  implicit val format: OFormat[JourneyOnFoot] = Json.format[JourneyOnFoot]
}

object JourneyInSmallVehicle {
  implicit val format: OFormat[JourneyInSmallVehicle] = Json.format[JourneyInSmallVehicle]
}

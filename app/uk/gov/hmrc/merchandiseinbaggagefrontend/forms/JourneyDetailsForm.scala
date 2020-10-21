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

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import java.time.LocalDate

import play.api.data.Form
import play.api.data.Forms.{mapping, of}
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.mappings.{LocalDateFormatter, Mappings}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{JourneyDetailsEntry, Port, Ports}

object JourneyDetailsForm extends Mappings {
  val placeOfArrival = "placeOfArrival"
  val dateOfArrival = "dateOfArrival"

  private val dateErrorKey = "journeyDetails.dateOfArrival.error"
  private val placeErrorKey = "journeyDetails.placeOfArrival.error"

  private val localDate = of(new LocalDateFormatter(s"$dateErrorKey.invalid"))

  private val withinTheNextFiveDays = Constraint { value: LocalDate =>
    val today = LocalDate.now
    val fiveDaysTime = today.plusDays(5)

    if (value.isBefore(today) || value.isAfter(fiveDaysTime)) Invalid(s"$dateErrorKey.notWithinTheNext5Days")
    else Valid
  }

  val form: Form[JourneyDetailsEntry] = Form(
    mapping(
      placeOfArrival -> enum[Port](Ports, s"$placeErrorKey.required", s"$placeErrorKey.invalid"),
      dateOfArrival -> localDate.verifying(withinTheNextFiveDays)
    )(JourneyDetailsEntry.apply)(JourneyDetailsEntry.unapply)
  )
}

/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.forms

import java.time.LocalDate

import play.api.data.Form
import play.api.data.Forms.{mapping, of}
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.{LocalDateFormatter, Mappings}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyDetailsEntry
import uk.gov.hmrc.merchandiseinbaggage.service.PortService

object JourneyDetailsForm extends Mappings {
  val port = "port"
  val dateOfTravel = "dateOfTravel"

  private val dateErrorKey = "journeyDetails.dateOfTravel.error"
  private val portErrorKey = "journeyDetails.port.error"

  private val localDate = of(new LocalDateFormatter(s"$dateErrorKey.invalid"))

  private val dateValidation: (LocalDate, DeclarationType) => Constraint[LocalDate] =
    (declarationDate, declarationType) =>
      Constraint { value: LocalDate =>
        (isPastAnd2020(value, declarationDate), afterFiveDays(value, declarationDate), isPastWithin30Days(value, declarationDate)) match {
          case (true, _, _) => Invalid(s"$dateErrorKey.$declarationType.dateInPast")
          case (_, _, true) => Invalid(s"$dateErrorKey.$declarationType.dateInPast.within.30.days")
          case (_, true, _) => Invalid(s"$dateErrorKey.notWithinTheNext5Days")
          case _            => Valid
        }
    }

  def form(declarationType: DeclarationType, today: LocalDate = LocalDate.now): Form[JourneyDetailsEntry] = Form(
    mapping(
      port -> text(s"$portErrorKey.$declarationType.required")
        .verifying(s"$portErrorKey.$declarationType.invalid", code => PortService.isValidPortCode(code)),
      dateOfTravel -> localDate.verifying(dateValidation(today, declarationType))
    )(JourneyDetailsEntry.apply)(JourneyDetailsEntry.unapply)
  )

  private def afterFiveDays(value: LocalDate, today: LocalDate): Boolean = value.isAfter(today.plusDays(5))

  private def isPastAnd2020(value: LocalDate, today: LocalDate): Boolean = value.isBefore(today) && value.getYear < 2021

  private def isPastWithin30Days(value: LocalDate, today: LocalDate): Boolean =
    value.isBefore(today) && today.getDayOfYear - value.getDayOfYear > 30
}

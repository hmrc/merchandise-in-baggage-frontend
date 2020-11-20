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

package uk.gov.hmrc.merchandiseinbaggage.forms

import java.time.LocalDate

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggage.forms.JourneyDetailsForm._
import uk.gov.hmrc.merchandiseinbaggage.forms.behaviours.FieldBehaviours
import uk.gov.hmrc.merchandiseinbaggage.model.core.Ports.Dover
import uk.gov.hmrc.merchandiseinbaggage.model.core.{JourneyDetailsEntry, Ports}

class JourneyDetailsFormSpec extends FieldBehaviours {
  private val today = LocalDate.now()

  placeOfArrival must {
    val requiredMessageKey = "journeyDetails.placeOfArrival.error.required"
    val invalidMessageKey = "journeyDetails.placeOfArrival.error.invalid"

    behave like mandatoryField(form, placeOfArrival, FormError(placeOfArrival, requiredMessageKey))
    behave like anEnumField(form, placeOfArrival, Ports, invalidMessageKey)
  }

  dateOfArrival must {
    val next5DaysMessageKey = "journeyDetails.dateOfArrival.error.notWithinTheNext5Days"

    behave like aMandatoryDateField(form, dateOfArrival)
    behave like aDateFieldWithMin(form, dateOfArrival, today, next5DaysMessageKey)
    behave like aDateFieldWithMax(form, dateOfArrival, today.plusDays(5), next5DaysMessageKey)
  }

  "form" must {
    "bind a place and date of arrival to a JourneyDetailsEntry" in {
      form.bind(
        Map(
          placeOfArrival -> Dover.entryName,
          s"$dateOfArrival.day"   -> today.getDayOfMonth.toString,
          s"$dateOfArrival.month" -> today.getMonthValue.toString,
          s"$dateOfArrival.year"  -> today.getYear.toString )).value.get mustBe JourneyDetailsEntry(Dover, today)
    }
  }
}

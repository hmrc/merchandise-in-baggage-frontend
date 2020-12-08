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
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.core.JourneyDetailsEntry

class JourneyDetailsFormSpec extends FieldBehaviours {
  private val today = LocalDate.now()

  port must {
    val importRequiredMessageKey = "journeyDetails.port.error.Import.required"
    val exportRequiredMessageKey = "journeyDetails.port.error.Export.required"

    behave like mandatoryField(form(Import), port, FormError(port, importRequiredMessageKey))
    behave like mandatoryField(form(Export), port, FormError(port, exportRequiredMessageKey))
  }

  dateOfTravel must {
    val next5DaysMessageKey = "journeyDetails.dateOfTravel.error.notWithinTheNext5Days"
    val dateInPastMessageKey = "journeyDetails.dateOfTravel.error.dateInPast"

    behave like aMandatoryDateField(form(Import), dateOfTravel)
    behave like aDateFieldWithMin(form(Import), dateOfTravel, today, dateInPastMessageKey)
    behave like aDateFieldWithMax(form(Import), dateOfTravel, today.plusDays(5), next5DaysMessageKey)
  }

  "form" must {
    "bind a place and date of arrival to a JourneyDetailsEntry" in {
      form(Import).bind(
        Map(
          port -> "DVR",
          s"$dateOfTravel.day"   -> today.getDayOfMonth.toString,
          s"$dateOfTravel.month" -> today.getMonthValue.toString,
          s"$dateOfTravel.year"  -> today.getYear.toString )).value.get mustBe JourneyDetailsEntry("DVR", today)
    }
  }
}

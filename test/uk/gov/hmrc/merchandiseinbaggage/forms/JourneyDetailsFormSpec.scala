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

package uk.gov.hmrc.merchandiseinbaggage.forms

import play.api.data.{Form, FormError}
import uk.gov.hmrc.merchandiseinbaggage.forms.JourneyDetailsForm._
import uk.gov.hmrc.merchandiseinbaggage.forms.behaviours.FieldBehaviours
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, JourneyDetailsEntry}

import java.time.LocalDate

class JourneyDetailsFormSpec extends FieldBehaviours {
  private val firstJanuary         = LocalDate.of(2021, 1, 1)
  private val next5DaysMessageKey  = "journeyDetails.dateOfTravel.error.notWithinTheNext5Days"
  private val dateInPastMessageKey = "journeyDetails.dateOfTravel.error.Import.dateInPast"

  port must {
    val importRequiredMessageKey = "journeyDetails.port.error.Import.required"
    val exportRequiredMessageKey = "journeyDetails.port.error.Export.required"

    behave like mandatoryField(form(Import), port, FormError(port, importRequiredMessageKey))
    behave like mandatoryField(form(Export), port, FormError(port, exportRequiredMessageKey))
  }

  "form" must {
    "bind a place and date of arrival to a JourneyDetailsEntry" in {
      form(Import, firstJanuary).bind(formData(firstJanuary)).value.get mustBe JourneyDetailsEntry("DVR", firstJanuary)
    }

    s"return $next5DaysMessageKey error when more than 5 days ahead" in {
      val formSubmission: DeclarationType => Form[JourneyDetailsEntry] =
        declarationType => form(declarationType, firstJanuary).bind(formData(firstJanuary.plusDays(6)))

      formSubmission(Import).error(dateOfTravel).get.message mustBe next5DaysMessageKey
    }

    "bind a place and date of declaration retrospectively for year 2021 to a JourneyDetailsEntry" in {
      val formSubmission: DeclarationType => Form[JourneyDetailsEntry] =
        declarationType => form(declarationType, firstJanuary.plusDays(1)).bind(formData(firstJanuary))

      formSubmission(Import).errors mustBe Seq.empty
      formSubmission(Import).value mustBe Some(JourneyDetailsEntry("DVR", firstJanuary))
      formSubmission(Export).errors mustBe Seq.empty
      formSubmission(Export).value mustBe Some(JourneyDetailsEntry("DVR", firstJanuary))
    }

    "bind date of declaration retrospectively for year 2021 but restrict the earliest date allowable to be 1/1/21" in {
      val dateFromInPastFrom2020 = LocalDate.of(2020, 12, 31)
      val importSubmittedForm    = form(Import, firstJanuary.plusDays(1)).bind(formData(dateFromInPastFrom2020))
      val exportSubmittedForm    = form(Export, firstJanuary.plusDays(1)).bind(formData(dateFromInPastFrom2020))

      importSubmittedForm.errors.head.message mustBe dateInPastMessageKey
      exportSubmittedForm.errors.head.message mustBe "journeyDetails.dateOfTravel.error.Export.dateInPast"
    }

    "bind date of declaration retrospectively for year 2021 but restrict the earliest date allowable to be 1/1/21 and within 30 days" in {
      val dateFromInPastFrom2020 = LocalDate.of(2021, 1, 1)
      val thirtyFirstJanuary     = firstJanuary.plusDays(31)
      val importSubmittedForm    = form(Import, thirtyFirstJanuary).bind(formData(dateFromInPastFrom2020))
      val exportSubmittedForm    = form(Export, thirtyFirstJanuary).bind(formData(dateFromInPastFrom2020))

      importSubmittedForm.errors.head.message mustBe s"$dateInPastMessageKey.within.30.days"
      exportSubmittedForm.errors.head.message mustBe "journeyDetails.dateOfTravel.error.Export.dateInPast.within.30.days"
    }
  }

  private val formData: LocalDate => Map[String, String] = dateOfArrival =>
    Map(
      port                   -> "DVR",
      s"$dateOfTravel.day"   -> dateOfArrival.getDayOfMonth.toString,
      s"$dateOfTravel.month" -> dateOfArrival.getMonthValue.toString,
      s"$dateOfTravel.year"  -> dateOfArrival.getYear.toString
    )
}

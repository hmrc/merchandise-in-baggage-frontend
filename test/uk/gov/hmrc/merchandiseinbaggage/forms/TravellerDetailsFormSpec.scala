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

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggage.forms.TravellerDetailsForm._
import uk.gov.hmrc.merchandiseinbaggage.forms.behaviours.FieldBehaviours

class TravellerDetailsFormSpec extends FieldBehaviours {
  private val nameMatchingRegex = "aZ'- "
  private val numbersAndSpecialCharacters =
    Set("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "!", ".", ",", "$")

  firstName must {
    val requiredMessageKey = "travellerDetails.firstName.error.required"
    val invalidMessageKey = "travellerDetails.firstName.error.invalid"

    behave like mandatoryField(form, firstName, FormError(firstName, requiredMessageKey))

    "include letters a to z, apostrophes, hyphens and spaces only" in {
      form.bind(Map(firstName -> nameMatchingRegex, lastName -> nameMatchingRegex)).errors mustBe Seq.empty

      numbersAndSpecialCharacters.foreach { invalidName =>
        form.bind(Map(firstName -> invalidName, lastName -> nameMatchingRegex)).errors mustBe
          Seq(FormError(firstName, List(invalidMessageKey)))
      }
    }
  }

  lastName must {
    val requiredMessageKey = "travellerDetails.lastName.error.required"
    val invalidMessageKey = "travellerDetails.lastName.error.invalid"

    behave like mandatoryField(form, lastName, FormError(lastName, requiredMessageKey))

    "include letters a to z, apostrophes, hyphens and spaces only" in {
      form.bind(Map(firstName -> nameMatchingRegex, lastName -> nameMatchingRegex)).errors mustBe Seq.empty

      numbersAndSpecialCharacters.foreach { invalidName =>
        form.bind(Map(firstName -> nameMatchingRegex, lastName -> invalidName)).errors mustBe
          Seq(FormError(lastName, List(invalidMessageKey)))
      }
    }
  }
}

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

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggage.forms.RetrieveDeclarationForm.form
import uk.gov.hmrc.merchandiseinbaggage.forms.behaviours.FieldBehaviours

class RetrieveDeclarationFormSpec extends FieldBehaviours {

  "form" should {

    val requiredMibRefMessageKey = "retrieveDeclaration.mibReference.error.required"
    val requiredEoriMessageKey = "retrieveDeclaration.eori.error.required"

    behave like mandatoryField(form, "mibReference", FormError("mibReference", requiredMibRefMessageKey))
    behave like mandatoryField(form, "eori", FormError("eori", requiredEoriMessageKey))

    "bind valid values" in {
      form.bind(Map("mibReference" -> "XAMB0000010000", "eori" -> "GB123456780000")).errors mustBe Seq.empty
    }

    "return form errors for any invalid data" in {
      form.bind(Map("mibReference" -> "XAMB00000100", "eori" -> "GB1234567800")).errors mustBe
        Seq(
          FormError("mibReference", "retrieveDeclaration.mibReference.error.invalid"),
          FormError("eori", "retrieveDeclaration.eori.error.invalid"))
    }
  }
}

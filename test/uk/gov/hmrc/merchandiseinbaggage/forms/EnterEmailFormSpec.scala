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

import play.api.data.{Form, FormError}
import uk.gov.hmrc.merchandiseinbaggage.forms.EnterEmailForm.form
import uk.gov.hmrc.merchandiseinbaggage.forms.behaviours.FieldBehaviours
import uk.gov.hmrc.merchandiseinbaggage.model.core.Email

class EnterEmailFormSpec extends FieldBehaviours {

  ".email" must {
    val fieldName = "email"

    behave like mandatoryEmailField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "error.required")
    )
  }

  ".confirmation" must {
    val fieldName = "confirmation"
    val requiredKey = "error.required"

    behave like mandatoryEmailField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "the emails" must {
    "match" in {
      val notMatch: Form[Email] = form.bind(Map("email" -> "test@test.com", "confirmation" -> "example@test.com"))
      val doesMatch: Form[Email] = form.bind(Map("email" -> "test@test.com", "confirmation" -> "test@test.com"))

      notMatch.errors mustBe Seq(FormError("", "enterEmail.error.notMatching"))
      doesMatch.errors mustBe Seq.empty

    }
  }
}

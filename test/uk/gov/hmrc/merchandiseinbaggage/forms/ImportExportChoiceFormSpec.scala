/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.forms.ImportExportChoiceForm.form
import uk.gov.hmrc.merchandiseinbaggage.forms.behaviours.FieldBehaviours
import uk.gov.hmrc.merchandiseinbaggage.model.core.ImportExportChoices

class ImportExportChoiceFormSpec extends FieldBehaviours {

  val requiredKey = "importExportChoice.error.required"
  val invalidKey  = "importExportChoice.error.required"

  ".value" must {

    val fieldName = "value"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, invalidKey)
    )

    "bind all valid values" in {
      ImportExportChoices.values.foreach { value =>
        form.bind(Map(fieldName -> value.toString)).errors mustBe Seq.empty
      }
    }

    "return error for invalid values" in {
      form.bind(Map(fieldName -> "invalid_value")).errors mustBe Seq(FormError(fieldName, invalidKey))
    }
  }

}

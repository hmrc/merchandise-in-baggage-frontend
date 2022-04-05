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

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggage.forms.NewOrExistingForm.form
import uk.gov.hmrc.merchandiseinbaggage.forms.behaviours.OptionFieldBehaviours
import uk.gov.hmrc.merchandiseinbaggage.model.api.{JourneyType, JourneyTypes}

class NewOrExistingFormSpec extends OptionFieldBehaviours {

  ".value" must {

    val fieldName = "value"
    val requiredKey = "newOrExisting.error.required"

    behave like optionsField[JourneyType](
      form,
      fieldName,
      validValues = JourneyTypes.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}

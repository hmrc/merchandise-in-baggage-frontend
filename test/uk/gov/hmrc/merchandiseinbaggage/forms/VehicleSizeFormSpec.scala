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
import uk.gov.hmrc.merchandiseinbaggage.forms.VehicleSizeForm.form
import uk.gov.hmrc.merchandiseinbaggage.forms.behaviours.YesNoFieldBehaviours
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}

class VehicleSizeFormSpec extends YesNoFieldBehaviours {

  val importRequiredKey = "vehicleSize.error.Import.required"
  val exportRequiredKey = "vehicleSize.error.Export.required"
  val invalidKey        = "error.yesNo"

  ".value" must {

    val fieldName = "value"

    behave like yesNoField(
      form(Import),
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form(Import),
      fieldName,
      requiredError = FormError(fieldName, importRequiredKey)
    )

    behave like mandatoryField(
      form(Export),
      fieldName,
      requiredError = FormError(fieldName, exportRequiredKey)
    )
  }
}

/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.forms.EoriNumberForm.form
import uk.gov.hmrc.merchandiseinbaggage.forms.behaviours.FieldBehaviours
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}

class EoriNumberFormSpec extends FieldBehaviours {
  ".value" must {
    val fieldName = "eori"

    behave like mandatoryField(
      form(No, Import),
      fieldName,
      requiredError = FormError(fieldName, "eoriNumber.trader.Import.error.required")
    )

    behave like mandatoryField(
      form(Yes, Import),
      fieldName,
      requiredError = FormError(fieldName, "eoriNumber.agent.Import.error.required")
    )

    behave like mandatoryField(
      form(No, Export),
      fieldName,
      requiredError = FormError(fieldName, "eoriNumber.trader.Export.error.required")
    )

    behave like mandatoryField(
      form(Yes, Export),
      fieldName,
      requiredError = FormError(fieldName, "eoriNumber.agent.Export.error.required")
    )
  }
}

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

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.behaviours.BigDecimalFieldBehaviours

class PurchaseDetailsFormProviderSpec extends BigDecimalFieldBehaviours {

  val form = new PurchaseDetailsFormProvider()()

  ".price" must {

    val fieldName = "price"
    val requiredKey = "purchaseDetails.price.error.required"
    val invalidKey = "purchaseDetails.price.error.invalid"

    behave like bigDecimalField(
      form,
      fieldName,
      error = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".currency" must {

    val fieldName = "currency"
    val requiredKey = "purchaseDetails.currency.error.required"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}

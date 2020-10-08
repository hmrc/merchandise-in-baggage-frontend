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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.PurchaseDetailsForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.behaviours.FieldBehaviours

class PurchaseDetailsFormSpec extends FieldBehaviours {
  ".price" must {
    val fieldName = "price"

    "not bind non-numeric numbers" in {
      forAll(nonNumerics -> "nonNumeric") { nonNumeric =>
        val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
        result.errors mustEqual Seq(FormError(fieldName, "purchaseDetails.price.error.invalid"))
      }
    }

    "bind all big decimal values with decimal places <= 3" in {
      forAll(positiveBigDecimalsWith3dp -> "bigDecimals") { bigDecimal: BigDecimal =>
        val result = form.bind(Map(fieldName -> bigDecimal.toString)).apply(fieldName)
        result.errors mustEqual Seq.empty
      }
    }

    "not bind all big decimal values with decimal places > 3" in {
      forAll(positiveBigDecimalsWithMoreThan3dp -> "bigDecimals") { bigDecimal: BigDecimal =>
        val result = form.bind(Map(fieldName -> bigDecimal.toString)).apply(fieldName)
        result.errors mustEqual Seq(FormError(fieldName, "error.max.3.decimals"))
      }
    }

    "not bind zero or negative numbers" in {
      forAll(zeroOrNegativeBigDecimalsWith3dp -> "bigDecimals") { bigDecimal: BigDecimal =>
        val result = form.bind(Map(fieldName -> bigDecimal.toString)).apply(fieldName)
        result.errors mustEqual Seq(FormError(fieldName, "error.must.be.positive"))
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "purchaseDetails.price.error.required")
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

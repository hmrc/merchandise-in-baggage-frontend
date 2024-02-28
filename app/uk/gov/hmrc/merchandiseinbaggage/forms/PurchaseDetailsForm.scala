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

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.Mappings
import uk.gov.hmrc.merchandiseinbaggage.model.core.PurchaseDetailsInput

import scala.util.{Success, Try}

object PurchaseDetailsForm extends Mappings {
  private val isAValidPurchasePrice: Constraint[String] = Constraint { value =>
    Try(BigDecimal(value)) match {
      case Success(bigDecimal) =>
        if (bigDecimal <= 0) {
          Invalid("error.must.be.positive")
        } else if (bigDecimal.scale > 3) {
          Invalid("error.max.3.decimals")
        } else {
          Valid
        }
      case _                   =>
        Invalid("purchaseDetails.price.error.invalid")
    }
  }

  val form: Form[PurchaseDetailsInput] =
    Form(
      mapping(
        "price"    -> text("purchaseDetails.price.error.required").verifying(isAValidPurchasePrice),
        "currency" -> text("purchaseDetails.currency.error.required")
      )(PurchaseDetailsInput.apply)(PurchaseDetailsInput.unapply)
    )
}

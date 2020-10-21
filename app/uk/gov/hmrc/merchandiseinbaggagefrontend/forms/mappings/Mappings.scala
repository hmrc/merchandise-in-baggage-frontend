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

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms.mappings

import enumeratum.EnumEntry
import play.api.data.FieldMapping
import play.api.data.Forms.of
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.Enum
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required"): FieldMapping[String] =
    of(stringFormatter(errorKey))

  protected def boolean(requiredKey: String = "error.required",
                        invalidKey: String = "error.boolean"): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey))

  protected def yesNo(requiredKey: String = "error.required",
                        invalidKey: String = "error.boolean"): FieldMapping[YesNo] =
    of(yesNoFormatter(requiredKey, invalidKey))

  protected def bigDecimal(requiredKey: String = "error.required", nonNumericKey: String = "error.nonNumeric"): FieldMapping[BigDecimal] =
    of(bigDecimalFormatter(requiredKey, nonNumericKey))

  protected def enum[A <: EnumEntry](enum: Enum[A], requiredKey: String = "error.required", invalidKey: String = "error.invalid"): FieldMapping[A] =
    of(enumFormatter[A](enum, requiredKey, invalidKey))
}

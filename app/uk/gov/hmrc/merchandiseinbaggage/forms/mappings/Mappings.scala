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

package uk.gov.hmrc.merchandiseinbaggage.forms.mappings

import java.time.LocalDate

import enumeratum.EnumEntry
import play.api.data.FieldMapping
import play.api.data.Forms.of
import uk.gov.hmrc.merchandiseinbaggage.model.api.Enum
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo

trait Mappings extends Formatters {

  private val requiredKey = "error.required"
  private val nonNumericKey = "error.nonNumeric"

  protected def text(errorKey: String = requiredKey): FieldMapping[String] =
    of(stringFormatter(errorKey))

  protected def localDate(invalidKey: String): FieldMapping[LocalDate] = of(new LocalDateFormatter(invalidKey))

  protected def yesNo(requiredKey: String = requiredKey, invalidKey: String = "error.yesNo"): FieldMapping[YesNo] =
    of(enumFormatter[YesNo](YesNo, requiredKey, invalidKey))

  protected def bigDecimal(requiredKey: String = requiredKey, nonNumericKey: String = nonNumericKey): FieldMapping[BigDecimal] =
    of(bigDecimalFormatter(requiredKey, nonNumericKey))

  protected def enum[A <: EnumEntry](
    enum: Enum[A],
    requiredKey: String = requiredKey,
    invalidKey: String = "error.invalid"): FieldMapping[A] =
    of(enumFormatter[A](enum, requiredKey, invalidKey))
}

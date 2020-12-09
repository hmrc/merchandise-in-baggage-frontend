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

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.Mappings
import uk.gov.hmrc.merchandiseinbaggage.model.core.CategoryQuantityOfGoods

object GoodsTypeQuantityForm extends Mappings {

  private val regex: String = "[0-9 A-Za-z]*"

  private def isValidQuantity(invalidMessageKey: String): Constraint[String] = Constraint { value: String =>
    if (value.trim matches regex) Valid
    else Invalid(invalidMessageKey)
  }

  val form: Form[CategoryQuantityOfGoods] =
    Form(
      mapping(
        "category" -> text("goodsTypeQuantity.category.error.required"),
        "quantity" ->
          text("goodsTypeQuantity.quantity.error.required")
          .verifying(isValidQuantity("goodsTypeQuantity.quantity.error.invalid"))
      )(CategoryQuantityOfGoods.apply)(CategoryQuantityOfGoods.unapply)
    )

}

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

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggage.forms.ValueWeightOfGoodsForm.form
import uk.gov.hmrc.merchandiseinbaggage.forms.behaviours.YesNoFieldBehaviours
import uk.gov.hmrc.merchandiseinbaggage.model.core.GoodsDestinations.{GreatBritain, NorthernIreland}

class ValueWeightOfGoodsFormSpec extends YesNoFieldBehaviours {

  val requiredKeyWhenGoodsDestinationIsGreatBritain = "valueWeightOfGoods.GreatBritain.error.required"
  val requiredKeyWhenGoodsDestinationIsNorthernIreland = "valueWeightOfGoods.NorthernIreland.error.required"
  val invalidKey = "error.yesNo"

  ".value" must {

    val fieldName = "value"

    behave like yesNoField(
      form(GreatBritain),
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form(GreatBritain),
      fieldName,
      requiredError = FormError(fieldName, requiredKeyWhenGoodsDestinationIsGreatBritain)
    )

    behave like mandatoryField(
      form(NorthernIreland),
      fieldName,
      requiredError = FormError(fieldName, requiredKeyWhenGoodsDestinationIsNorthernIreland)
    )
  }
}

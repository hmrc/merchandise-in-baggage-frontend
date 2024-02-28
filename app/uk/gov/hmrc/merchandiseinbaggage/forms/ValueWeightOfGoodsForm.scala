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
import play.api.i18n.Messages
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.Mappings
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestination
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched.AmountInPenceEnriched

object ValueWeightOfGoodsForm extends Mappings {

  def form(goodsDestination: GoodsDestination)(implicit messages: Messages): Form[YesNo] =
    Form(
      "value" -> yesNo(
        Messages(
          s"valueWeightOfGoods.${goodsDestination.entryName}.error.required",
          goodsDestination.threshold.formattedInPounds
        )
      )
    )

}

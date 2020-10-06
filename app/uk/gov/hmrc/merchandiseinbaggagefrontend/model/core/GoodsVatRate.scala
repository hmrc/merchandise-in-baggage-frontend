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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.core

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.{Enumerable, WithName}

trait GoodsVatRate

object GoodsVatRate extends Enumerable.Implicits {

  case object Zero extends WithName("zero") with GoodsVatRate
  case object Five extends WithName("five") with GoodsVatRate
  case object Twenty extends WithName("twenty") with GoodsVatRate

  val values: Seq[GoodsVatRate] = Seq(
    Zero,
    Five,
    Twenty
  )

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map { value =>
    RadioItem(
      value = Some(value.toString),
      content = Text(messages(s"goodsVatRate.${value.toString}")),
      checked = form("value").value.contains(value.toString)
    )
  }

  implicit val enumerable: Enumerable[GoodsVatRate] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

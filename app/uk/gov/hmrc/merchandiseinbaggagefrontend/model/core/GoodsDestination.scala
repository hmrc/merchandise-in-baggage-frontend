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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.{Enumerable, WithName}

sealed trait GoodsDestination

object GoodsDestination extends Enumerable.Implicits {

  case object NorthernIreland extends WithName("ni") with GoodsDestination
  case object EngScoWal extends WithName("gb") with GoodsDestination

  val values: Seq[GoodsDestination] = Seq(
    NorthernIreland,
    EngScoWal
  )

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map { value =>
    RadioItem(
      value = Some(value.toString),
      content =
        if (value == NorthernIreland)
          Text(messages(s"goodsDestination.${value.toString}"))
        else
          //TODO this is YUK!
          HtmlContent(s"""
                ${Text(messages(s"goodsDestination.${value.toString}")).asHtml}
                <div id="sign-in-item-hint" class="govuk-hint govuk-radios__hint" style="padding-left:0px">
                  ${messages(s"goodsDestination.gb.hint")}
                </div>"""),
      checked = form("value").value.contains(value.toString)
    )
  }


  implicit val enumerable: Enumerable[GoodsDestination] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

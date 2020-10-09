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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model

import enumeratum.{EnumEntry, PlayEnum}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{Format, JsError, JsString, JsSuccess, Reads, Writes}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

trait Enum[A <: EnumEntry] extends PlayEnum[A] {
  val baseMessageKey: String

  def forCode(code: String): Option[A] = values.find(_.toString == code)

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map { value =>
    RadioItem(
      value = Some(value.toString),
      content = Text(messages(s"$baseMessageKey.${value.toString}")),
      checked = form("value").value.contains(value.toString),
      hint = hint(value)
    )
  }

  protected def hint(value: A)(implicit messages: Messages): Option[Hint] = None
}

object EnumFormat {
  def apply[T <: EnumEntry](e: Enum[T]): Format[T] = Format(
    Reads {
      case JsString(value) => e.withNameOption(value).map(JsSuccess(_)).getOrElse(JsError(s"Unknown ${e.getClass.getSimpleName} value: $value"))
      case _ => JsError("Can only parse String")
    },
    Writes(v => JsString(v.entryName)))
}



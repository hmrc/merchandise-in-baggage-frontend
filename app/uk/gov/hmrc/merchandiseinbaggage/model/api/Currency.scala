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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}

case class Currency(code: String, displayName: String, valueForConversion: Option[String], currencySynonyms: List[String]) {

  def toAutoCompleteJson(implicit messages: Messages): JsObject =
    Json.obj("code" -> code, "displayName" -> messages(displayName), "synonyms" -> currencySynonyms)

}

object Currency {
  implicit val formats = Json.format[Currency]
}

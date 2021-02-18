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

import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue}

sealed trait PaymentStatus

case object Paid extends PaymentStatus

case object NotRequired extends PaymentStatus

object PaymentStatus {
  implicit val format: Format[PaymentStatus] = new Format[PaymentStatus] {
    override def reads(json: JsValue): JsResult[PaymentStatus] =
      json.as[String] match {
        case "Paid"        => JsSuccess(Paid)
        case "NotRequired" => JsSuccess(NotRequired)
      }

    override def writes(o: PaymentStatus): JsValue = o match {
      case Paid        => JsString("Paid")
      case NotRequired => JsString("NotRequired")
    }
  }
}

/*
 * Copyright 2025 HM Revenue & Customs
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

import java.time.LocalDateTime

import play.api.libs.json.{Json, OFormat}

case class Amendment(
  reference: Int,
  dateOfAmendment: LocalDateTime,
  goods: DeclarationGoods,
  maybeTotalCalculationResult: Option[TotalCalculationResult],
  paymentStatus: Option[PaymentStatus],
  source: Option[String],
  lang: String,
  emailsSent: Boolean
)

object Amendment {
  implicit val format: OFormat[Amendment] = Json.format[Amendment]

  def apply(
    reference: Int,
    dateOfAmendment: LocalDateTime,
    goods: DeclarationGoods,
    maybeTotalCalculationResult: Option[TotalCalculationResult] = None,
    paymentStatus: Option[PaymentStatus] = None,
    source: Option[String] = None,
    emailsSent: Boolean = false
  ): Amendment =
    Amendment(reference, dateOfAmendment, goods, maybeTotalCalculationResult, paymentStatus, source, "en", emailsSent)
}

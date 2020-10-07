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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion

import java.time.LocalDate

import play.api.libs.json.{Json, OFormat}

case class Currency(countryName: String, currencyName: String, currencyCode: String) {
  def displayName = s"${countryName.trim} ${currencyName.trim} (${currencyCode.trim})"
}

object Currency {
  implicit val format: OFormat[Currency] = Json.format[Currency]
}

case class CurrencyPeriod(start: LocalDate, end: LocalDate, currencies: Seq[Currency])

object CurrencyPeriod {
  implicit val format: OFormat[CurrencyPeriod] = Json.format[CurrencyPeriod]
}

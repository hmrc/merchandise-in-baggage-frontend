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

package uk.gov.hmrc.merchandiseinbaggagefrontend.connectors

import java.time.LocalDate

import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.CurrencyConversionConf
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.CurrencyPeriod
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson

import scala.concurrent.{ExecutionContext, Future}

trait CurrencyConversionConnector extends CurrencyConversionConf {
  val httpClient: HttpClient

  def getCurrencies()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CurrencyPeriod] =
    httpClient.GET[CurrencyPeriod](s"$currencyConversionBaseUrl/currency-conversion/currencies/${LocalDate.now()}")
}

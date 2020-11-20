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

package uk.gov.hmrc.merchandiseinbaggage.stubs

import java.time.LocalDate

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, get, okJson, urlPathEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object CurrencyConversionStub {
  val currencyConversionResponse: String =
    """{
      |  "start": "2020-10-01",
      |  "end": "2020-10-31",
      |  "currencies": [
      |    {
      |      "countryName": "Argentina",
      |      "currencyName": "Peso",
      |      "currencyCode": "ARS"
      |    },
      |    {
      |      "countryName": "Australia",
      |      "currencyName": "Dollar",
      |      "currencyCode": "AUD"
      |    },
      |    {
      |      "countryName": "Brazil",
      |      "currencyName": "Real",
      |      "currencyCode": "BRL"
      |    },
      |    {
      |      "countryName": "Eurozone",
      |      "currencyName": "Euro",
      |      "currencyCode": "EUR"
      |    }
      |  ]
      |}
      |""".stripMargin

  def conversionRateResponse(code: String) =
    s"""[
       |  {
       |    "startDate": "2020-10-01",
       |    "endDate": "2020-10-31",
       |    "currencyCode": "$code",
       |    "rate": "1.2763"
       |  }
       |]""".stripMargin


  def givenCurrenciesAreFound(server: WireMockServer): StubMapping =
    server
      .stubFor(get(urlPathEqualTo(s"/currency-conversion/currencies/${LocalDate.now()}"))
      .willReturn(okJson(currencyConversionResponse).withStatus(200)))

  def givenCurrencyIsFound(code: String, server: WireMockServer): StubMapping =
    server
      .stubFor(get(urlPathEqualTo(s"/currency-conversion/rates/${LocalDate.now()}"))
        .withQueryParam("cc", equalTo(code))
      .willReturn(okJson(conversionRateResponse(code))))
}

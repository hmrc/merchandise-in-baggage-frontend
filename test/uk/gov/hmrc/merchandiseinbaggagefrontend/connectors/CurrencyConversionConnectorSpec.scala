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

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Second, Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.{ConversionRatePeriod, CurrencyPeriod}
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.CurrencyConversionStub._
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpecWithApplication, BaseSpecWithWireMock}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class CurrencyConversionConnectorSpec extends BaseSpecWithApplication with BaseSpecWithWireMock with Eventually {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(1L, Second)))

  "get list of currencies for a given date" in new CurrencyConversionConnector {
    override val httpClient: HttpClient = injector.instanceOf[HttpClient]

    override lazy val currencyConversionBaseUrl =
      s"${currencyConversionConf.protocol}://${currencyConversionConf.host}:${BaseSpecWithWireMock.port}"

    givenCurrenciesAreFound(wireMockServer)

    eventually {
      val response: CurrencyPeriod = Await.result(getCurrencies(), 5.seconds)

      response mustBe Json.parse(currencyConversionResponse).as[CurrencyPeriod]
    }
  }

  "get conversion rate for a given currency code" in new CurrencyConversionConnector {
    override val httpClient: HttpClient = injector.instanceOf[HttpClient]

    override lazy val currencyConversionBaseUrl =
      s"${currencyConversionConf.protocol}://${currencyConversionConf.host}:${BaseSpecWithWireMock.port}"

    givenCurrencyIsFound("USD", wireMockServer)

    eventually {
      val response: Seq[ConversionRatePeriod] = Await.result(getConversionRate("USD"), 5.seconds)

      response mustBe Json.parse(conversionRateResponse("USD")).as[Seq[ConversionRatePeriod]]
    }
  }

}

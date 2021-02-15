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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import com.github.tomakehurst.wiremock.client.WireMock.{post, urlPathEqualTo, _}
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.connectors.PaymentApiUrls._
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{JourneyId, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.URL
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global

class PaymentConnectorSpec extends BaseSpecWithApplication with WireMockSupport with CoreTestData {

  class TestPaymentConnector
      extends PaymentConnector(injector.instanceOf[HttpClient], injector.instanceOf[ServicesConfig].baseUrl("payment"))

  "send a payment request to payment service adding a generated session id to the header" in new TestPaymentConnector {
    val stubbedResponse = s"""{"journeyId":"5f3bc55","nextUrl":"http://localhost:9056/pay/initiate-journey"}"""

    implicit val hc: HeaderCarrier = HeaderCarrier()

    wireMockServer
      .stubFor(
        post(urlPathEqualTo(payUrl))
          .withRequestBody(equalToJson(toJson(payApiRequest).toString, true, false))
          .willReturn(okJson(stubbedResponse).withStatus(201)))

    val response = sendPaymentRequest(payApiRequest).futureValue
    response mustBe PayApiResponse(JourneyId("5f3bc55"), URL("http://localhost:9056/pay/initiate-journey"))
  }
}

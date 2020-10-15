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

import com.github.tomakehurst.wiremock.client.WireMock.{post, urlPathEqualTo, _}
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.HeaderNames.xSessionId
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.URL
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpecWithWireMock, CoreTestData}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global

class PaymentConnectorSpec extends BaseSpecWithWireMock with CoreTestData {

  class TestPaymentConnector extends PaymentConnector(
    injector.instanceOf[HttpClient], injector.instanceOf[ServicesConfig].baseUrl("payment"))

  "send a payment request to payment service adding a generated session id to the header" in new TestPaymentConnector {
    private val sessionId = generateSessionId
    private val stubbedResponse = s"""{"journeyId":"5f3bc55","nextUrl":"http://localhost:9056/pay/initiate-journey"}"""

    private implicit val hc: HeaderCarrier = HeaderCarrier()

    override def addSessionId(headerCarrier: HeaderCarrier): HeaderCarrier = hc.withExtraHeaders(xSessionId -> sessionId)

    wireMockServer
      .stubFor(post(urlPathEqualTo(s"/pay-api/mib-frontend/mib/journey/start"))
        .withRequestBody(equalToJson(toJson(payApiRequest).toString, true, false))
        .withHeader(xSessionId, containing(sessionId))
        .willReturn(okJson(stubbedResponse).withStatus(201))
      )

    val response: HttpResponse = makePayment(payApiRequest).futureValue
    response.status mustBe 201
    response.body mustBe stubbedResponse
  }

  "extract redirect url from pay-api http response" in new TestPaymentConnector {
    private val payApiResponse = s"""{"journeyId":"1234","nextUrl":"http://something"}"""

    extractUrl(HttpResponse(201, payApiResponse)) mustBe PayApiResponse(JourneyId("1234"), URL("http://something"))
  }
}

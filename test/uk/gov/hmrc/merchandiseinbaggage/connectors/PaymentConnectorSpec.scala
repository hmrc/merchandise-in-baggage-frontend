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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{JourneyId, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.URL
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global

class PaymentConnectorSpec extends BaseSpecWithApplication with CoreTestData {

  class TestPaymentConnector extends PaymentConnector(appConfig, injector.instanceOf[HttpClientV2])

  "send a payment request to payment service adding a generated session id to the header" in new TestPaymentConnector {
    val stubbedResponse = s"""{"journeyId":"5f3bc55","nextUrl":"http://localhost:9056/pay/initiate-journey"}"""

    wireMockServer
      .stubFor(
        post(urlPathEqualTo("/pay-api/mib-frontend/mib/journey/start"))
          .withRequestBody(equalToJson(toJson(payApiRequest).toString, true, false))
          .willReturn(okJson(stubbedResponse).withStatus(Status.CREATED))
      )

    val response: PayApiResponse = sendPaymentRequest(payApiRequest).futureValue
    response mustBe PayApiResponse(JourneyId("5f3bc55"), URL("http://localhost:9056/pay/initiate-journey"))
  }
}

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

package uk.gov.hmrc.merchandiseinbaggagefrontend.service

import com.github.tomakehurst.wiremock.client.WireMock.{post, urlPathEqualTo, _}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Second, Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.URL
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpecWithWireMock, CoreTestData}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class PaymentServiceSpec extends BaseSpecWithWireMock with Eventually with CoreTestData {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(1L, Second)))

  "send a payment request to payment service adding a generated session id to the header" in new PaymentService {
    private val sessionId = generateSessionId
    override def addSessionId(headerCarrier: HeaderCarrier): HeaderCarrier =
      hc.withExtraHeaders(HeaderNames.xSessionId -> sessionId)

    val stubbedResponse = s"""{"journeyId":"5f3bc55","nextUrl":"http://localhost:9056/pay/initiate-journey"}"""

    paymentMockServer
      .stubFor(post(urlPathEqualTo(s"/${paymentServiceConf.url.value}"))
      .withRequestBody(equalToJson(Json.toJson(payApiRequest).toString, true, false))
      .withHeader(HeaderNames.xSessionId, containing(sessionId))
      .willReturn(okJson(stubbedResponse).withStatus(201))
    )

    private val httpClient = app.injector.instanceOf[HttpClient]

    eventually {
      val response: HttpResponse = Await.result(makePayment(httpClient, payApiRequest), 5.seconds)
      response.status mustBe 201
      response.body mustBe stubbedResponse
    }
  }

  "extract redirect url from pay-api http response" in new PaymentService {
    val payApiResponse = s"""{"journeyId":"1234","nextUrl":"http://something"}"""

    extractUrl(HttpResponse(201, payApiResponse)) mustBe PayApiResponse(JourneyId("1234"), URL("http://something"))
  }
}

/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers.service

import com.github.tomakehurst.wiremock.client.WireMock.{post, urlPathEqualTo, _}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Second, Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.URL
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.PaymentService
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpec, BaseSpecWithWireMock, CoreTestData}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class PaymentServiceSpec extends BaseSpec with BaseSpecWithWireMock with Eventually with CoreTestData {

  implicit val hc = HeaderCarrier()
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(1L, Second)))

  "send a payment request to payment service adding a generated session id to the header" in new PaymentService {
    val stubbedSessionId = generateSessionId
    override def addSessionId(headerCarrier: HeaderCarrier): HeaderCarrier =
      hc.withExtraHeaders(HeaderNames.xSessionId -> stubbedSessionId)

    val stubbedResponse = s"""{"journeyId":"5f3bc55","nextUrl":"http://localhost:9056/pay/initiate-journey"}"""

    paymentMockServer
      .stubFor(post(urlPathEqualTo(s"/${paymentServiceConf.url.value}"))
      .withRequestBody(equalToJson(Json.toJson(payApiRequest).toString, true, false))
      .withHeader(HeaderNames.xSessionId, containing(stubbedSessionId))
      .willReturn(okJson(stubbedResponse).withStatus(201))
    )

    val httpClient = app.injector.instanceOf[HttpClient]

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

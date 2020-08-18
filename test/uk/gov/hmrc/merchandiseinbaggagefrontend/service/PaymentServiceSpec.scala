/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers.service

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlPathEqualTo, _}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Second, Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpec, BaseSpecWithWireMock}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.{AmountInPence, JourneyId, MerchandiseDetails, MibReference, PayApiResponse, PaymentRequest, TraderDetails}
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.PaymentService

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class PaymentServiceSpec extends BaseSpec with BaseSpecWithWireMock with Eventually {

  implicit val hc = HeaderCarrier().withExtraHeaders(HeaderNames.xSessionId -> "789")
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(1L, Second)))

  "send a payment request to payment service" in new PaymentService {
    val paymentUrl = s"/${paymentServiceConf.url.value}"
    val body = PaymentRequest(
      MibReference("MIBI1234567890"),
      AmountInPence(1),
      AmountInPence(2),
      AmountInPence(3),
      TraderDetails("Trader Inc, 239 Old Street, Berlin, Germany, EC1V 9EY"),
      MerchandiseDetails("Parts and technical crew for the forest moon")
    )

    val journeyId = """"5f3bc55b220100c2207edc69""""
    val redirectUrl = """"http://localhost:9056/pay/initiate-journey?traceId=53661661""""
    val stubbedResponse = s"""{"journeyId":"5f3bc55b220100c2207edc69","nextUrl":$redirectUrl}"""

    paymentMockServer
      .stubFor(post(urlPathEqualTo(paymentUrl))
      .withRequestBody(equalToJson(Json.toJson(body).toString, true, false))
      .willReturn(okJson(stubbedResponse).withStatus(201))
    )

    val httpClient = app.injector.instanceOf[HttpClient]

    eventually {
      val response: HttpResponse = Await.result(makePayment(httpClient, body), 5.seconds)
      response.status mustBe 201
      response.body mustBe stubbedResponse
    }
  }

  "extract redirect url from pay-api http response" in new PaymentService {
    val payApiResponse = s"""{"journeyId":"1234","nextUrl":"http://something"}"""

    extractUrl(HttpResponse(201, payApiResponse)) mustBe PayApiResponse(JourneyId("1234"), URL("http://something"))
  }
}

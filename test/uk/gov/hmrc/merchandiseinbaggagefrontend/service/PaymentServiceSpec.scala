/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers.service

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlPathEqualTo, _}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Second, Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggagefrontend.controllers.{BaseSpec, BaseSpecWithWireMock}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model._
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.PaymentService

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class PaymentServiceSpec extends BaseSpec with BaseSpecWithWireMock with Eventually {

  implicit val hc = HeaderCarrier()
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(1L, Second)))

  "send a payment request to payment service" in new PaymentService {
    val paymentUrl = s"/${paymentServiceConf.url.value}"
    val body = PaymentRequest(
      ChargeReference("MIBI1234567890"),
      TaxToPayInPence(200099),
      TraderDetails("Trader Inc, 239 Old Street, Berlin, Germany, EC1V 9EY"),
      MerchandiseDetails("Parts and technical crew for the forest moon")
    )
    paymentMockServer
      .stubFor(post(urlPathEqualTo(paymentUrl))
      .withRequestBody(equalToJson(Json.toJson(body).toString, true, false))
      .willReturn(aResponse().withStatus(200))
    )

    val httpClient = app.injector.instanceOf[HttpClient]

    eventually {
      val response = Await.result(makePayment(httpClient, body), 5.seconds)
      response.status mustBe 200
    }
  }
}

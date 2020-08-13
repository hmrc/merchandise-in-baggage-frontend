/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers.service

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlPathEqualTo}
import uk.gov.hmrc.merchandiseinbaggagefrontend.PaymentService
import uk.gov.hmrc.merchandiseinbaggagefrontend.controllers.{BaseSpec, BaseSpecWithWireMock}

import scala.concurrent.Future
import scala.util.Success

class PaymentServiceSpec extends BaseSpec with BaseSpecWithWireMock {

  "send a payment request to payment service" in new PaymentService {
    private val paymentUrl = "/payment"
    paymentMockServer.stubFor(get(urlPathEqualTo(paymentUrl))
      .willReturn(aResponse().withStatus(200)))

    val stubbedGet: String => Future[String] = _ => Future.successful("")


    makePayment(stubbedGet, paymentUrl).value mustBe Some(Success(""))
  }

}

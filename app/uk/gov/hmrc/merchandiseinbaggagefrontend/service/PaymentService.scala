/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.service

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.PaymentServiceConf
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.{PayApiResponse, PaymentRequest}

import scala.concurrent.{ExecutionContext, Future}

trait PaymentService extends PaymentServiceConf {

  def makePayment(httpClient: HttpClient, requestBody: PaymentRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClient.POST(s"$paymentBaseUri${paymentServiceConf.url.value}", Json.toJson(requestBody))

  protected def extractUrl(response: HttpResponse): PayApiResponse =
    Json.parse(response.body).as[PayApiResponse]
}

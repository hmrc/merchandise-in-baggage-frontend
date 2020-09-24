/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.service

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.PaymentServiceConf
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.{PayApiResponse, PayApitRequest}
import uk.gov.hmrc.merchandiseinbaggagefrontend.utils.SessionIdGenerator
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

trait PaymentService extends PaymentServiceConf with SessionIdGenerator {

  def makePayment(httpClient: HttpClient, requestBody: PayApitRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    httpClient.POST[PayApitRequest, HttpResponse](s"$paymentBaseUri${paymentServiceConf.url.value}", requestBody, addSessionId(hc).headers)
  }

  protected def extractUrl(response: HttpResponse): PayApiResponse =
    Json.parse(response.body).as[PayApiResponse]
}

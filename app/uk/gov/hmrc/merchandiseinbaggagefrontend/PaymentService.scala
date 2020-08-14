/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend

import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.PaymentServiceConf

import scala.concurrent.{ExecutionContext, Future}

trait PaymentService extends PaymentServiceConf {

  def makePayment(httpClient: HttpClient, url: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClient.POST(s"$paymentBaseUri$url", "")
}

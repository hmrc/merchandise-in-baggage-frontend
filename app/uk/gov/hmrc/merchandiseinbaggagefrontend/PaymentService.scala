/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend

import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

trait PaymentService {

  def makePayment(doGet: HttpClient, url: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    doGet.POST(s"http://localhost:9662$url", "")

}

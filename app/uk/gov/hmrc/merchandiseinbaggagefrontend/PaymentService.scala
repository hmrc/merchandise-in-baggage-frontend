/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend

import scala.concurrent.Future

trait PaymentService {

  def makePayment(doGet: String => Future[String], url: String): Future[String] = doGet(url)

}

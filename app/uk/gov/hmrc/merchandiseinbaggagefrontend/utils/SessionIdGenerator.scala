/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.utils

import java.util.UUID

import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}

trait SessionIdGenerator {

  def addSessionId(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders(HeaderNames.xSessionId -> generateSessionId)

  protected def generateSessionId: String = UUID.randomUUID().toString
}

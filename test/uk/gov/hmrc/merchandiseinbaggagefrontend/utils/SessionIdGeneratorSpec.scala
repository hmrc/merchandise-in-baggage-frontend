/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.utils

import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpec

class SessionIdGeneratorSpec extends BaseSpec {

  "generate new sessionId as a String" in new SessionIdGenerator {
    generateSessionId mustBe a [String]
  }

  "add a generated sessionId to a given header carrier" in new SessionIdGenerator {
    val headerCarrier: HeaderCarrier = HeaderCarrier()

    addSessionId(headerCarrier).headers.map(_._1) must contain(HeaderNames.xSessionId)
  }
}

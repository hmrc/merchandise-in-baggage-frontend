/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggage.utils

import org.scalamock.scalatest.MockFactory
import play.api.mvc.{Cookies, Headers, RequestHeader}
import play.api.mvc.request.RequestTarget
import uk.gov.hmrc.merchandiseinbaggage.BaseSpec

class DeclarationJourneyLoggerSpec extends BaseSpec with MockFactory {

  "DeclarationJourneyLogger" should {
    "be able to warn" in {

      implicit val mockRequest = mock[RequestHeader]

      val mockTarget = mock[RequestTarget]

      (mockTarget.path _).stubs().returns("PATH")

      (mockRequest.target _).stubs().returns(mockTarget)
      (mockRequest.method _).stubs().returns("METHOD")
      (mockRequest.headers _).stubs().returns(Headers.create())
      (mockRequest.cookies _).stubs().returns(Cookies(Seq()))

      DeclarationJourneyLogger.warn("Test")
    }
  }
}

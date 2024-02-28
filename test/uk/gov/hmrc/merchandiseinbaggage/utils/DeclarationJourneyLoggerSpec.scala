/*
 * Copyright 2024 HM Revenue & Customs
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

import org.mockito.MockitoSugar.{mock, when}
import play.api.mvc.request.RequestTarget
import play.api.mvc.{Cookies, Headers, RequestHeader}
import uk.gov.hmrc.merchandiseinbaggage.BaseSpec

class DeclarationJourneyLoggerSpec extends BaseSpec {

  "DeclarationJourneyLogger" should {
    "be able to warn" in {

      implicit val mockRequest: RequestHeader = mock[RequestHeader]

      val mockTarget = mock[RequestTarget]

      when(mockTarget.path).thenReturn("PATH")

      when(mockRequest.target).thenReturn(mockTarget)
      when(mockRequest.method).thenReturn("METHOD")
      when(mockRequest.headers).thenReturn(Headers.create())
      when(mockRequest.cookies).thenReturn(Cookies(Seq()))

      DeclarationJourneyLogger.warn("Test")

      //TODO Should there be an assertion for this or remove this test
    }
  }
}

/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.config

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

class ErrorHandlerSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {
  private val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  "ErrorHandler" must {
    "return an error page" in {
      val result = errorHandler
        .standardErrorTemplate(
          pageTitle = "pageTitle",
          heading = "heading",
          message = "message"
        )(fakeRequest)
        .futureValue

      result.body must include("pageTitle")
      result.body must include("heading")
      result.body must include("message")
    }
  }

}

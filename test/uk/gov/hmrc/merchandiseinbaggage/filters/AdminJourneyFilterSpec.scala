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

package uk.gov.hmrc.merchandiseinbaggage.filters

import play.api.{Application, inject}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.headers
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication
import play.api.test.Helpers.defaultAwaitTimeout
import scala.concurrent.Future

class AdminJourneyFilterSpec extends BaseSpecWithApplication {
  def fakeAdminApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      Map(
        "adminJourneyFilter.enabled" -> true
      )
    )
    .build()

  def fakefn(x: RequestHeader): Future[Result] = Future.successful(Results.Ok.withHeaders(x.headers.headers*))
  "AdminJourneyFilter" when {
    "admin" in {
      val adminJourneyFilter = fakeAdminApplication().injector.instanceOf[AdminJourneyFilter]
      val fakeRequest        = FakeRequest().withHeaders(("abc", "def"))
      val header             = headers(adminJourneyFilter.apply(fakefn)(fakeRequest))
      header must contain allElementsOf Map("x-forwarded-host" -> "admin.tax.service.gov.uk", "abc" -> "def")
    }
    "public" in {
      val publicJourneyFilter = fakeApplication().injector.instanceOf[AdminJourneyFilter]
      val fakeRequest         = FakeRequest().withHeaders(("abc", "def"))
      val header              = headers(publicJourneyFilter.apply(fakefn)(fakeRequest))
      header must contain allElementsOf Map("abc" -> "def")
      header must contain noElementsOf Map("x-forwarded-host" -> "admin.tax.service.gov.uk")
    }
  }
}

/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs

import com.softwaremill.macwire.wire
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.SessionExpiredPage
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.SessionExpiredPage.path

class SessionExpiredPageSpec extends BasePageSpec[SessionExpiredPage] {
  override lazy val page: SessionExpiredPage = wire[SessionExpiredPage]

  "the session expired page render" in {
    open(path)
    println(messageApi)

    page.mustRenderBasicContent(path, messageApi("sessionExpired.title"))
    page.headerText mustBe messages("sessionExpired.heading")
    page.textOfElementWithId("expiredGuidanceId") mustBe messages("sessionExpired.guidance")
    page.textOfElementWithId("expiredRestartId") mustBe messages("sessionExpired.restart.p")
    page.textOfElementWithId("expiredUlIdOne") mustBe messages("sessionExpired.Import.restart")
    page.textOfElementWithId("expiredUlIdTwo") mustBe messages("taking commercial goods out of Great Britain")
  }
}

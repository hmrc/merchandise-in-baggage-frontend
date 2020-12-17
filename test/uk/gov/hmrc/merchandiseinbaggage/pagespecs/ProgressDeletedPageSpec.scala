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
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.ProgressDeletedPage
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.ProgressDeletedPage.path

class ProgressDeletedPageSpec extends BasePageSpec[ProgressDeletedPage] {
  override lazy val page: ProgressDeletedPage = wire[ProgressDeletedPage]

  "the progress deleted page render" in {
    open(path)

    page.mustRenderBasicContent(path, messageApi("progressDeleted.title"))
    page.headerText mustBe messages("progressDeleted.heading")
    page.textOfElementWithId("expiredRestartId") mustBe messages("progressDeleted.restart.p")
    page.textOfElementWithId("expiredUlIdOne") mustBe messages("progressDeleted.Import.restart")
    page.textOfElementWithId("expiredUlIdTwo") mustBe messages("progressDeleted.Export.restart")
  }
}

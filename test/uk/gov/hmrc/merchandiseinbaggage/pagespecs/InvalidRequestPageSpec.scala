/*
 * Copyright 2021 HM Revenue & Customs
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

import org.openqa.selenium.By
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.InvalidRequestPage
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.SearchGoodsCountryPage.path

class InvalidRequestPageSpec extends BasePageSpec[InvalidRequestPage]   {
  override def page: InvalidRequestPage = new InvalidRequestPage

  def findLink(elm:String ):String =
    page.element(elm).findElement(By.tagName("a")).getAttribute("href")

  "the page should link to start-import" in {
    open(path(1))
    findLink("expiredUlIdOne") must include("/declare-commercial-goods/start-import")
  }

  "the page should link to start-export" in {
    open(path(1))
    findLink("expiredUlIdTwo") must include("/declare-commercial-goods/start-export")
  }
}

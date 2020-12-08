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
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.GoodsRemovedPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{GoodsDestinationPage, GoodsRemovedPage, GoodsTypeQuantityPage, StartExportPage, StartImportPage}

class GoodsRemovedPageSpec extends BasePageSpec[GoodsRemovedPage] {
  override def page: GoodsRemovedPage = wire[GoodsRemovedPage]

  "the goods removed page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), title)
    behave like aPageWithNoBackButton(path)

    s"redirect to ${GoodsTypeQuantityPage.path(1)}" when {
      "the user elects to add more goods" in {
        givenAnImportJourneyIsStarted()

        open(path)
        page.addMoreGoods() mustBe GoodsTypeQuantityPage.path(1)
      }
    }

    s"redirect to ${StartImportPage.path}" when {
      "the user elects to start again after an import journey" in {
        givenAnImportJourneyIsStarted()

        open(path)
        page.startAgain() mustBe GoodsDestinationPage.path
      }
    }

    s"redirect to ${StartExportPage.path}" when {
      "the user elects to start again after an export journey" in {
        givenAnExportJourneyIsStarted()

        open(path)
        page.startAgain() mustBe GoodsDestinationPage.path
      }
    }
  }
}

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

package uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{GoodsEntry, YesNo}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.RemoveGoodsPage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{GoodsRemovedPage, RadioButtonPage, ReviewGoodsPage}

import scala.concurrent.ExecutionContext.Implicits.global

class RemoveGoodsPageSpec extends BasePageSpec[RadioButtonPage[YesNo]] {
  override def page: RadioButtonPage[YesNo] = removeGoodsPage

  "the remove goods page" should {
    behave like aPageWhichRequiresADeclarationJourney(path(1))
    behave like aPageWhichRequiresADeclarationJourney(path(2))
    behave like aPageWhichRenders(path(1), givenAGoodsEntryIsComplete(), title("wine"))
    behave like aPageWhichRenders(path(2), givenTwoGoodsEntriesAreComplete(), title("cheese"))

    s"redirect to ${ReviewGoodsPage.path}" when {
      s"the user elects not to remove the goods" in {
        givenAGoodsEntryIsComplete()
        open(path(1))
        page.fillOutForm(No)
        page.clickOnCTA() mustBe ReviewGoodsPage.path
      }
    }

    s"remove the goods entry and redirect to ${GoodsRemovedPage.path}" when {
      s"the user elects to remove a goods entry" in {
        givenTwoGoodsEntriesAreComplete()

        open(path(2))
        page.fillOutForm(Yes)
        page.clickOnCTA() mustBe ReviewGoodsPage.path

        declarationJourneyRepository.findAll().futureValue.head.goodsEntries.entries mustBe Seq(completedGoodsEntry)
      }

      s"the user elects to remove the only goods entry" in {
        givenAGoodsEntryIsComplete()
        open(path(1))
        page.fillOutForm(Yes)
        page.clickOnCTA() mustBe GoodsRemovedPage.path

        declarationJourneyRepository.findAll().futureValue.head.goodsEntries.entries mustBe Seq(GoodsEntry.empty)
      }
    }
  }
}

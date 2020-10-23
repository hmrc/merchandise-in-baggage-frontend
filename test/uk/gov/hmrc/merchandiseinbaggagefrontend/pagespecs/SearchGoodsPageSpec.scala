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

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{CategoryQuantityOfGoods, GoodsEntry}

class SearchGoodsPageSpec extends BasePageSpec {

  "/search-goods/:idx" should {
    "render correctly" when {
      "a declaration has been started" in {
        startImportJourney()

        searchGoodsPage.open()
        searchGoodsPage.mustRenderBasicContent()
      }

      "a declaration has been completed" in {
        val expected = completedDeclarationJourney.goodsEntries.entries.head.maybeCategoryQuantityOfGoods.get
        createDeclarationJourney(completedDeclarationJourney)

        searchGoodsPage.open()
        searchGoodsPage.mustRenderBasicContent()
        searchGoodsPage.previouslyEnteredValuesAreDisplayed(expected)
      }
    }

    "redirect to /goods-vat-rate" when {
      "form is submitted for the first time" in {
        val input = completedDeclarationJourney.goodsEntries.entries.head.maybeCategoryQuantityOfGoods.get

        startImportJourney()

        searchGoodsPage.open()
        searchGoodsPage.fillOutForm(input)
        searchGoodsPage.clickOnSubmitButtonMustRedirectTo("/merchandise-in-baggage/goods-vat-rate/1")

        declarationJourneyRepository
          .findBySessionId(sessionId)
          .futureValue
          .get
          .goodsEntries
          .entries
          .head mustBe GoodsEntry(Some(input))
      }
    }

    "redirect to /review-goods" when {
      "form is submitted as part of a change" in {
        val input = CategoryQuantityOfGoods("clothes", "10")

        createDeclarationJourney(completedDeclarationJourney)

        val before = completedDeclarationJourney.goodsEntries.entries.head.maybeCategoryQuantityOfGoods.get

        declarationJourneyRepository
          .findBySessionId(sessionId)
          .futureValue
          .get
          .goodsEntries
          .entries
          .head
          .maybeCategoryQuantityOfGoods
          .get mustBe before

        searchGoodsPage.open()
        searchGoodsPage.fillOutForm(input)
        searchGoodsPage.clickOnSubmitButtonMustRedirectTo("/merchandise-in-baggage/review-goods")

        declarationJourneyRepository
          .findBySessionId(sessionId)
          .futureValue
          .get
          .goodsEntries
          .entries
          .head
          .maybeCategoryQuantityOfGoods
          .get mustBe input
      }
    }
  }
}

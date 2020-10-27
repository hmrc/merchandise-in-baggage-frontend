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

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{CategoryQuantityOfGoods, DeclarationJourney}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.SearchGoodsPage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{GoodsVatRatePage, ReviewGoodsPage, SearchGoodsPage}

class SearchGoodsPageSpec extends DeclarationDataCapturePageSpec[CategoryQuantityOfGoods, SearchGoodsPage] {
  override def page: SearchGoodsPage = searchGoodsPage

  "the search goods page" should {
    val path = SearchGoodsPage.path()
    val categoryQuantityOfGoods = CategoryQuantityOfGoods("test good", "123")

    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)

    behave like aDataCapturePageWithConditionalRouting(
      path, givenAnImportJourneyIsStarted(), categoryQuantityOfGoods, GoodsVatRatePage.path())

    behave like aDataCapturePageWithConditionalRouting(
      path, givenACompleteDeclarationJourney(), categoryQuantityOfGoods, ReviewGoodsPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[CategoryQuantityOfGoods] =
    declarationJourney.goodsEntries.entries.head.maybeCategoryQuantityOfGoods
}

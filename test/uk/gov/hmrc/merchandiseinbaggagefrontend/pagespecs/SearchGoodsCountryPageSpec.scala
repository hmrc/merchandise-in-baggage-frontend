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

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.SearchGoodsCountryPage.title
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{PurchaseDetailsPage, SearchGoodsCountryPage}
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CountriesService.countries

class SearchGoodsCountryPageSpec extends DeclarationDataCapturePageSpec[String, SearchGoodsCountryPage] with ScalaFutures {
  override lazy val page: SearchGoodsCountryPage = searchGoodsCountryPage

  "the search goods country page" should {
    val path = SearchGoodsCountryPage.path()
    def givenAGoodsEntryIsStarted(): Unit = givenADeclarationJourney(declarationJourneyWithStartedGoodsEntry)

    behave like aPageWhichRenders(path, givenAGoodsEntryIsStarted(), title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aDataCapturePageWithSimpleRouting(path, givenAGoodsEntryIsStarted(), countries.take(3), PurchaseDetailsPage.path())
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[String] =
    declarationJourney.goodsEntries.entries.head.maybeCountryOfPurchase
}

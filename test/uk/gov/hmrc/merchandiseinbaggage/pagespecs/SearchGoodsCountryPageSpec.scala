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
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.core.GoodsEntry
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.SearchGoodsCountryPage.{path, title}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{GoodsVatRatePage, PurchaseDetailsPage, SearchGoodsCountryPage}
import uk.gov.hmrc.merchandiseinbaggage.service.CountriesService.countries

class SearchGoodsCountryPageSpec extends GoodsEntryPageSpec[String, SearchGoodsCountryPage] with ScalaFutures {
  override lazy val page: SearchGoodsCountryPage = wire[SearchGoodsCountryPage]

  private val validationMessage = "Select the country where you bought the goods"

  "the search goods country page" should {
    behave like aGoodsEntryPage(path, title, countries.head, Some(PurchaseDetailsPage.path), GoodsVatRatePage.path)
    behave like aPageWhichDisplaysValidationErrorMessagesInTheErrorSummary(path(1), Set(validationMessage), givenAGoodsEntryIsStarted())
  }

  override def extractFormDataFrom(goodsEntry: GoodsEntry): Option[String] = goodsEntry.maybeCountryOfPurchase
}

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
import uk.gov.hmrc.merchandiseinbaggage.model.core.{GoodsEntry, PurchaseDetailsInput}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.PurchaseDetailsPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{PurchaseDetailsPage, SearchGoodsCountryPage}
import uk.gov.hmrc.merchandiseinbaggage.stubs.CurrencyConversionStub.givenCurrenciesAreFound

class PurchaseDetailsPageSpec extends GoodsEntryPageSpec[PurchaseDetailsInput, PurchaseDetailsPage] with ScalaFutures {
  override lazy val page: PurchaseDetailsPage = wire[PurchaseDetailsPage]

  override def beforeEach(): Unit = {
    super.beforeEach()
    givenCurrenciesAreFound(wireMockServer)
  }

  "the purchase details page" should {
    behave like aGoodsEntryPage(
      path, title, PurchaseDetailsInput("100", "EUR"), None, SearchGoodsCountryPage.path)
  }

  override def extractFormDataFrom(goodsEntry: GoodsEntry): Option[PurchaseDetailsInput] =
    goodsEntry.maybePurchaseDetails map (_.purchaseDetailsInput)
}
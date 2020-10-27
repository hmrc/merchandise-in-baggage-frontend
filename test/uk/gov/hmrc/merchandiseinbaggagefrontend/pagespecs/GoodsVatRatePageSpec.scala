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

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsVatRates.Twenty
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{GoodsEntry, GoodsVatRate}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.GoodsVatRatePage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{RadioButtonPage, SearchGoodsCountryPage}

class GoodsVatRatePageSpec extends GoodsEntryPageSpec[GoodsVatRate, RadioButtonPage[GoodsVatRate]] {
  override lazy val page: RadioButtonPage[GoodsVatRate] = goodsVatRatePage

  "the goods vat rate page" should {
    behave like aGoodsEntryPage(path, title, Twenty, Some(SearchGoodsCountryPage.path))
  }

  override def extractFormDataFrom(goodsEntry: GoodsEntry): Option[GoodsVatRate] = goodsEntry.maybeGoodsVatRate
}

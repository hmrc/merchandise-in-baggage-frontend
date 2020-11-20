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
import uk.gov.hmrc.merchandiseinbaggage.model.core.{CategoryQuantityOfGoods, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.GoodsTypeQuantityPage.{path, title}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{GoodsTypeQuantityPage, GoodsVatRatePage, ValueWeightOfGoodsPage}

class GoodsTypeQuantityPageSpec extends GoodsEntryPageSpec[CategoryQuantityOfGoods, GoodsTypeQuantityPage] {
  override def page: GoodsTypeQuantityPage = wire[GoodsTypeQuantityPage]

  "the goods type quantity page" should {
    val back: Int => String = _ => ValueWeightOfGoodsPage.path

    behave like aGoodsEntryPage(path, title, CategoryQuantityOfGoods("test good", "123"), Some(GoodsVatRatePage.path), back)
  }

  override def extractFormDataFrom(goodsEntry: GoodsEntry): Option[CategoryQuantityOfGoods] = goodsEntry.maybeCategoryQuantityOfGoods
}

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

import com.softwaremill.macwire.wire
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.core.{ExportGoodsEntry, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.SearchGoodsCountryPage
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.SearchGoodsCountryPage._

class SearchGoodsCountryPageSpec extends GoodsEntryPageSpec[String, SearchGoodsCountryPage] with ScalaFutures {
  override lazy val page: SearchGoodsCountryPage = wire[SearchGoodsCountryPage]

  "the search goods country page export" should {
    behave like aGoodEntryExportPageTitle(path(1), exportTitle(1))
  }

  override def extractFormDataFrom(goodsEntry: GoodsEntry): Option[String] =
    goodsEntry.asInstanceOf[ExportGoodsEntry].maybeDestination.map(_.code)
}

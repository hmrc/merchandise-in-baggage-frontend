/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.viewmodels

import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.ProgressDeletedPage.messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, SummaryList, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class GoodsItemListSpec extends BaseSpec with CoreTestData {
  "summaryList" in {
    GoodsItemList.summaryList(Seq(aImportGoods))(messages) mustBe Seq(
      SummaryList(
        rows = Seq(
          SummaryListRow(
            key = Key(Text(messages("reviewGoods.list.item"))),
            value = Value(Text(aImportGoods.category))
          ),
          SummaryListRow(
            key = Key(Text(messages("reviewGoods.list.producedInEu"))),
            value = Value(Text(messages(aImportGoods.producedInEu.messageKey)))
          ),
          SummaryListRow(
            key = Key(Text(messages("reviewGoods.list.price"))),
            value = Value(Text(messages(aImportGoods.purchaseDetails.formatted)))
          )
        ),
        classes = "govuk-!-margin-bottom-1"
      )
    )
  }
}

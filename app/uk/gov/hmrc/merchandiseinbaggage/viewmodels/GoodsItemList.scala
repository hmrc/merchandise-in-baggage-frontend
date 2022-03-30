/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.merchandiseinbaggage.model.api.{ExportGoods, Goods, ImportGoods}

object GoodsItemList {

  def summaryList(declarationGoods: Seq[Goods])(implicit messages: Messages): Seq[SummaryList] =
    declarationGoods.zipWithIndex.map { item =>
      val goods = item._1
      val idx = item._2 + 1

      goods match {
        case ig: ImportGoods => importSummary(ig, idx)
        case eg: ExportGoods => exportSummary(eg, idx)
      }
    }

  private def rowWithOutChange(key: String, value: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(Text(messages(key))),
      value = Value(Text(value)),
    )

  private def importSummary(goods: ImportGoods, idx: Int)(implicit messages: Messages): SummaryList =
    SummaryList(
      rows = Seq(
        rowWithOutChange(
          "reviewGoods.list.item",
          goods.category
        ),
        rowWithOutChange(
          "reviewGoods.list.producedInEu",
          messages(goods.producedInEu.messageKey)
        ),
        rowWithOutChange(
          "reviewGoods.list.price",
          goods.purchaseDetails.formatted
        )
      ),
      classes = "govuk-!-margin-bottom-1"
    )

  private def exportSummary(goods: ExportGoods, idx: Int)(implicit messages: Messages): SummaryList =
    SummaryList(
      rows = Seq(
        rowWithOutChange(
          "reviewGoods.list.item",
          goods.category
        ),
        rowWithOutChange(
          "reviewGoods.list.destination",
          messages(goods.destination.countryName)
        ),
        rowWithOutChange(
          "reviewGoods.list.price",
          goods.purchaseDetails.formatted
        )
      ),
      classes = "govuk-!-margin-bottom-1"
    )

}

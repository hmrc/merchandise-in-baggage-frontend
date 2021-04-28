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

package uk.gov.hmrc.merchandiseinbaggage.viewmodels

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, SummaryListRow}
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationGoods, ExportGoods, ImportGoods}

object GoodsSummaryList {

  def summaryList(declarationGoods: DeclarationGoods)(implicit messages: Messages): Seq[SummaryList] =
    declarationGoods.goods.zipWithIndex.map { item =>
      val goods = item._1
      val idx = item._2 + 1

      goods match {
        case ig: ImportGoods => importSummary(ig, idx)
        case eg: ExportGoods => exportSummary(eg, idx)
      }
    }

  private def rowWithChange(key: String, value: String, changeUrl: String, changeId: String, hiddenTxt: String)(
    implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(Text(messages(key))),
      value = Value(Text(value)),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href = changeUrl,
              content = Text(messages("site.change")),
              visuallyHiddenText = Some(messages(hiddenTxt)),
              attributes = Map("id" -> s"$changeId")
            )
          )
        ))
    )

  private def importSummary(goods: ImportGoods, idx: Int)(implicit messages: Messages): SummaryList =
    SummaryList(
      rows = Seq(
        rowWithChange(
          "reviewGoods.list.item",
          goods.category,
          routes.GoodsTypeController.onPageLoad(idx).url,
          s"categoryChangeLink_$idx",
          "reviewGoods.goodsType.changeText"
        ),
        rowWithChange(
          "reviewGoods.list.price",
          goods.purchaseDetails.formatted,
          routes.PurchaseDetailsController.onPageLoad(idx).url,
          s"priceChangeLink_$idx",
          "reviewGoods.price.changeText"
        ),
        rowWithChange(
          "reviewGoods.list.producedInEu",
          messages(goods.producedInEu.messageKey),
          routes.GoodsOriginController.onPageLoad(idx).url,
          s"goodsOriginChangeLink_$idx",
          "reviewGoods.country.changeText"
        ),
        rowWithChange(
          "reviewGoods.list.vatRate",
          s"${goods.goodsVatRate.value}%",
          routes.GoodsVatRateController.onPageLoad(idx).url,
          s"vatRateChangeLink_$idx",
          "reviewGoods.list.vatRate"
        )
      ),
      classes = "govuk-!-margin-bottom-1",
      attributes = Map("id" -> "summaryListId")
    )

  private def exportSummary(goods: ExportGoods, idx: Int)(implicit messages: Messages): SummaryList =
    SummaryList(
      rows = Seq(
        rowWithChange(
          "reviewGoods.list.item",
          goods.category,
          routes.GoodsTypeController.onPageLoad(idx).url,
          s"categoryChangeLink_$idx",
          "reviewGoods.goodsType.changeText"
        ),
        rowWithChange(
          "reviewGoods.list.price",
          goods.purchaseDetails.formatted,
          routes.PurchaseDetailsController.onPageLoad(idx).url,
          s"priceChangeLink_$idx",
          "reviewGoods.price.changeText"
        ),
        rowWithChange(
          "reviewGoods.list.destination",
          messages(goods.destination.countryName),
          routes.SearchGoodsCountryController.onPageLoad(idx).url,
          s"destinationChangeLink_$idx",
          "reviewGoods.destination.changeText"
        )
      ),
      classes = "govuk-!-margin-bottom-1"
    )

}

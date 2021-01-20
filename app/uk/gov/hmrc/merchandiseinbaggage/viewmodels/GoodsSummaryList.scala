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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationGoods, Goods}

object GoodsSummaryList {

  def summaryList(declarationGoods: DeclarationGoods, declarationType: DeclarationType)(implicit messages: Messages): Seq[SummaryList] =
    declarationGoods.goods.zipWithIndex.map { item =>
      val goods = item._1
      val idx = item._2 + 1

      declarationType match {
        case Import => importSummary(goods, idx)
        case Export => exportSummary(goods, idx)
      }
    }

  private def rowWithChange(key: String, value: String, changeUrl: String, changeId: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(Text(messages(key))),
      value = Value(Text(value)),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href = changeUrl,
              content = Text(messages("site.change")),
              visuallyHiddenText = Some(messages(key)),
              attributes = Map("id" -> s"$changeId")
            )
          )
        ))
    )

  private def importSummary(goods: Goods, idx: Int)(implicit messages: Messages): SummaryList =
    SummaryList(
      rows = Seq(
        rowWithChange(
          "reviewGoods.list.item",
          goods.categoryQuantityOfGoods.category,
          routes.GoodsTypeQuantityController.onPageLoad(idx).url,
          s"categoryChangeLink_$idx"
        ),
        rowWithChange(
          "reviewGoods.list.quantity",
          goods.categoryQuantityOfGoods.quantity,
          routes.GoodsTypeQuantityController.onPageLoad(idx).url,
          s"quantityChangeLink_$idx"
        ),
        rowWithChange(
          "reviewGoods.list.vatRate",
          s"${goods.goodsVatRate.value}%",
          routes.GoodsVatRateController.onPageLoad(idx).url,
          s"vatRateChangeLink_$idx"
        ),
        rowWithChange(
          "reviewGoods.list.country",
          messages(goods.countryOfPurchase.countryName),
          routes.SearchGoodsCountryController.onPageLoad(idx).url,
          s"countryChangeLink_$idx"
        ),
        rowWithChange(
          "reviewGoods.list.price",
          goods.purchaseDetails.formatted,
          routes.PurchaseDetailsController.onPageLoad(idx).url,
          s"priceChangeLink_$idx"
        )
      ),
      classes = "govuk-!-margin-bottom-1"
    )

  private def exportSummary(goods: Goods, idx: Int)(implicit messages: Messages): SummaryList =
    SummaryList(
      rows = Seq(
        rowWithChange(
          "reviewGoods.list.item",
          goods.categoryQuantityOfGoods.category,
          routes.GoodsTypeQuantityController.onPageLoad(idx).url,
          s"categoryChangeLink_$idx"
        ),
        rowWithChange(
          "reviewGoods.list.quantity",
          goods.categoryQuantityOfGoods.quantity,
          routes.GoodsTypeQuantityController.onPageLoad(idx).url,
          s"quantityChangeLink_$idx"
        ),
        rowWithChange(
          "reviewGoods.list.destination",
          messages(goods.countryOfPurchase.countryName),
          routes.SearchGoodsCountryController.onPageLoad(idx).url,
          s"countryChangeLink_$idx"
        ),
        rowWithChange(
          "reviewGoods.list.price",
          goods.purchaseDetails.formatted,
          routes.PurchaseDetailsController.onPageLoad(idx).url,
          s"priceChangeLink_$idx"
        )
      ),
      classes = "govuk-!-margin-bottom-1"
    )

}

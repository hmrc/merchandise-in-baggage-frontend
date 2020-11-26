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

package uk.gov.hmrc.merchandiseinbaggage.viewmodels

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Key, SummaryList, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, SummaryListRow}
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationGoods, DeclarationType, GoodsVatRate}

object ReviewGoodsSummary {

  private def vatKey(implicit messages: Messages) = Key(Text(messages("reviewGoods.list.vatRate")))

  def summaryList(goods: DeclarationGoods, declarationType: DeclarationType)(implicit messages: Messages): Seq[SummaryList] =
    goods.goods.zipWithIndex.map { item =>
      import item._1._
      val idx = item._2 + 1

      SummaryList(showVatIfApplies(declarationType, Seq(
        SummaryListRow(
          key = Key(Text(messages("reviewGoods.list.item"))),
          value = Value(Text(categoryQuantityOfGoods.category)),
          actions = Some(Actions(
            items = Seq(
              ActionItem(routes.GoodsTypeQuantityController.onPageLoad(idx).url, Text(messages("site.change")))
            )
          ))
        ),
        SummaryListRow(
          key = Key(Text(messages("reviewGoods.list.quantity"))),
          value = Value(Text(categoryQuantityOfGoods.quantity)),
          actions = Some(Actions(
            items = Seq(
              ActionItem(routes.GoodsTypeQuantityController.onPageLoad(idx).url, Text(messages("site.change")))
            )
          ))
        ),
        SummaryListRow(
          key = Key(Text(messages("reviewGoods.list.vatRate"))),
          value = Value(Text(s"${goodsVatRate.value}%")),
          actions = Some(Actions(
            items = Seq(
              ActionItem(routes.GoodsVatRateController.onPageLoad(idx).url, Text(messages("site.change")))
            )
          ))
        ),
        SummaryListRow(
          key = Key(Text(messages("reviewGoods.list.country"))),
          value = Value(Text(countryOfPurchase)),
          actions = Some(Actions(
            items = Seq(
              ActionItem(routes.SearchGoodsCountryController.onPageLoad(idx).url, Text(messages("site.change")))
            )
          ))
        ),
        SummaryListRow(
          key = Key(Text(messages("reviewGoods.list.price"))),
          value = Value(Text(purchaseDetails.toString)),
          actions = Some(Actions(
            items = Seq(
              ActionItem(routes.PurchaseDetailsController.onPageLoad(idx).url, Text(messages("site.change")))
            )
          ))
        ),
        SummaryListRow(
          key = Key(
            HtmlContent(s"""<a style="font-weight: 400" href="${routes.RemoveGoodsController.onPageLoad(idx).url}" class="govuk-link">${messages("site.remove")}</a>""")
          ),
          classes = "govuk-summary-list__row--no-border"
        )
      )))
    }

  private def showVatIfApplies(declarationType: DeclarationType, rows: Seq[SummaryListRow])
                        (implicit messages: Messages): Seq[SummaryListRow] =
    if(declarationType == Import) rows else rows.filterNot(_.key == vatKey)
}

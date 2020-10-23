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

package uk.gov.hmrc.merchandiseinbaggagefrontend.viewmodels

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Key, SummaryList, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.merchandiseinbaggagefrontend.controllers.routes
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationGoods

object ReviewGoodsSummary {

  def summaryList(goods: DeclarationGoods)(implicit messages: Messages): Seq[SummaryList] =
    goods.goods.zipWithIndex.map { item =>
      import item._1._
      val idx = item._2 + 1

      SummaryList(Seq(
        SummaryListRow(
          Key(Text(messages("reviewGoods.list.item"))),
          Value(Text(categoryQuantityOfGoods.category))
        ),
        SummaryListRow(
          Key(Text(messages("reviewGoods.list.quantity"))),
          Value(Text(categoryQuantityOfGoods.quantity))
        ),
        SummaryListRow(
          Key(Text(messages("reviewGoods.list.vatRate"))),
          Value(Text(s"${goodsVatRate.value}%"))
        ),
        SummaryListRow(
          Key(Text(messages("reviewGoods.list.country"))),
          Value(Text(countryOfPurchase))
        ),
        SummaryListRow(
          Key(Text(messages("reviewGoods.list.price"))),
          Value(Text(purchaseDetails.toString))
        ),
        SummaryListRow(
          Key(Text(messages("reviewGoods.list.invoice"))),
          Value(Text(invoiceNumber))
        ),
        SummaryListRow(
          key = Key(
            HtmlContent(s"""<a style="font-weight: 400" href="${routes.RemoveGoodsController.onPageLoad(idx).url}" class="govuk-link">${messages("site.remove")}</a>""")
          ),
          classes = "govuk-summary-list__row--no-border"
        )
      ))
    }

}

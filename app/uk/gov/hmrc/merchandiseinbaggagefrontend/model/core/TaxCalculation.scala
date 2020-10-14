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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.core

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Table, TableRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.HeadCell
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.calculation.CalculationResult

case class TaxCalculation(goods: Goods, calculationResult: CalculationResult)

case class TaxCalculations(taxCalculations: Seq[TaxCalculation]) {
  def totalGbpValue: AmountInPence = AmountInPence(
    taxCalculations.map(_.calculationResult.gbpAmount.value).sum
  )

  def totalTaxDue: AmountInPence = AmountInPence(
    taxCalculations.map(_.calculationResult.taxDue.value).sum
  )

  def toTable(implicit messages: Messages): Table = {
    val tableRows: Seq[Seq[TableRow]] = taxCalculations.map { tc =>
      Seq(
        TableRow(
          Text(tc.goods.categoryQuantityOfGoods.category)
        ),
        TableRow(
          Text(tc.calculationResult.duty.formattedInPounds)
        ),
        TableRow(
          Text(
            messages(
              "taxCalculation.table.col3.row",
              tc.calculationResult.vat.formattedInPounds,
              tc.goods.goodsVatRate.value
            )
          )
        ),
        TableRow(
          Text(tc.calculationResult.taxDue.formattedInPounds)
        )
      )
    } :+ Seq(
      TableRow(
        content = Text(messages("taxCalculation.table.total")),
        classes = "govuk-table__header",
        colspan = Some(3),
        attributes = Map("style" -> "border: none")
      ),
      TableRow(
        content = Text(totalTaxDue.formattedInPounds),
        classes = "govuk-table__header",
        attributes = Map("style" -> "border: none")
      )
    )

    Table(
      rows = tableRows,
      head = Some(Seq(
        HeadCell(
          Text(messages("taxCalculation.table.col1.head"))
        ),
        HeadCell(
          Text(messages("taxCalculation.table.col2.head"))
        ),
        HeadCell(
          Text(messages("taxCalculation.table.col3.head"))
        ),
        HeadCell(
          Text(messages("taxCalculation.table.col4.head"))
        )
      ))
    )
  }
}

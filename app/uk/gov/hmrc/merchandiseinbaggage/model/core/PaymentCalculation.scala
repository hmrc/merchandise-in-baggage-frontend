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

package uk.gov.hmrc.merchandiseinbaggage.model.core

import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Table, TableRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.HeadCell
import uk.gov.hmrc.merchandiseinbaggage.model.calculation.CalculationResult

case class PaymentCalculation(goods: Goods, calculationResult: CalculationResult)

object PaymentCalculation {
  implicit val format: OFormat[PaymentCalculation] = Json.format[PaymentCalculation]
}

case class TotalCalculationResult(paymentCalculations: PaymentCalculations,
                                  totalGbpValue: AmountInPence,
                                  totalTaxDue: AmountInPence,
                                  totalDutyDue: AmountInPence,
                                  totalVatDue: AmountInPence)

object TotalCalculationResult {
  implicit val format: OFormat[TotalCalculationResult] = Json.format[TotalCalculationResult]
}

case class PaymentCalculations(paymentCalculations: Seq[PaymentCalculation]) {
  def totalGbpValue: AmountInPence = AmountInPence(
    paymentCalculations.map(_.calculationResult.gbpAmount.value).sum
  )

  def totalTaxDue: AmountInPence = AmountInPence(
    paymentCalculations.map(_.calculationResult.taxDue.value).sum
  )

  def totalDutyDue: AmountInPence = AmountInPence(
    paymentCalculations.map(_.calculationResult.duty.value).sum
  )

  def totalVatDue: AmountInPence = AmountInPence(
    paymentCalculations.map(_.calculationResult.vat.value).sum
  )

  def totalCalculationResult: TotalCalculationResult =
    TotalCalculationResult(this, totalGbpValue, totalTaxDue, totalDutyDue, totalVatDue)

  def toTable(implicit messages: Messages): Table = {
    val tableRows: Seq[Seq[TableRow]] = paymentCalculations.map { tc =>
      Seq(
        TableRow(
          Text(tc.goods.categoryQuantityOfGoods.category)
        ),
        TableRow(
          Text(tc.calculationResult.gbpAmount.formattedInPoundsUI),
          classes = "govuk-table__cell--numeric"
        ),
        TableRow(
          Text(tc.calculationResult.duty.formattedInPoundsUI),
          classes = "govuk-table__cell--numeric"
        ),
        TableRow(
          Text(
            messages(
              "paymentCalculation.table.col3.row",
              tc.calculationResult.vat.formattedInPoundsUI,
              tc.goods.goodsVatRate.value
            )
          ),
          classes = "govuk-table__cell--numeric",
          attributes = Map("nowrap" -> "nowrap")
        ),
        TableRow(
          Text(tc.calculationResult.taxDue.formattedInPoundsUI),
          classes = "govuk-table__cell--numeric"
        )
      )
    } :+ Seq(
      TableRow(
        content = Text(messages("paymentCalculation.table.total")),
        classes = "govuk-table__header"
      ),
      TableRow(
        content = Text(totalTaxDue.formattedInPoundsUI),
        classes = "govuk-table__cell--numeric govuk-!-font-weight-bold",
        colspan = Some(4)
      )
    )

    Table(
      rows = tableRows,
      attributes = Map("style" -> "margin-bottom:60px"),
      head = Some(Seq(
        HeadCell(
          Text(messages("paymentCalculation.table.col1.head")),
          attributes = Map("nowrap" -> "nowrap")
        ),
        HeadCell(
          Text(messages("paymentCalculation.table.col2.head")),
          attributes = Map("nowrap" -> "nowrap")
        ),
        HeadCell(
          Text(messages("paymentCalculation.table.col3.head")),
          attributes = Map("nowrap" -> "nowrap")
        ),
        HeadCell(
          Text(messages("paymentCalculation.table.col4.head")),
          attributes = Map("nowrap" -> "nowrap")
        ),
        HeadCell(
          Text(messages("paymentCalculation.table.col5.head")),
          attributes = Map("nowrap" -> "nowrap")
        )
      ))
    )
  }
}

object PaymentCalculations {
  implicit val format: OFormat[PaymentCalculations] = Json.format[PaymentCalculations]
}

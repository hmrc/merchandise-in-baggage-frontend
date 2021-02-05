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

package uk.gov.hmrc.merchandiseinbaggage.utils

import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Table, TableRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.HeadCell
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Country, _}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.PurchaseDetailsInput

object DataModelEnriched {

  implicit class PurchaseDetailsEnriched(details: PurchaseDetails) {
    import details._

    val numericAmount: BigDecimal = BigDecimal(amount)

    def purchaseDetailsInput: PurchaseDetailsInput = PurchaseDetailsInput(amount, currency.code)
  }

  implicit class AmountInPenceEnriched(amountInPence: AmountInPence) {
    import amountInPence._
    val formattedInPoundsUI: String = formattedInPounds.split("\\.00")(0) //TODO not sure if necessary different formats
    lazy val isPaymentRequired: Boolean = inPounds.compareTo(BigDecimal(0.0)) > 0
    def fromBigDecimal(in: BigDecimal): AmountInPence = AmountInPence((in * 100).toLong)
  }

  implicit class BigDecimalToAmountInPenceEnriched(bigDecimal: BigDecimal) {
    def fromBigDecimal: AmountInPence = AmountInPence((bigDecimal * 100).toLong)
  }

  implicit class GoodsEnriched(goods: ImportGoods) {
    import goods._
    val calculationRequest: CalculationRequest =
      CalculationRequest(purchaseDetails.numericAmount, purchaseDetails.currency, producedInEu, goodsVatRate)
  }

  implicit class CountryEnriched(country: Country) {
    import country._
    def toAutoCompleteJson(implicit messages: Messages): JsObject =
      Json.obj("code" -> code, "displayName" -> messages(countryName), "synonyms" -> countrySynonyms)
  }

  implicit class CurrencyEnriched(currency: Currency) {
    import currency._
    def toAutoCompleteJson(implicit messages: Messages): JsObject =
      Json.obj("code" -> code, "displayName" -> messages(displayName), "synonyms" -> currencySynonyms)
  }

  implicit class PortEnriched(port: Port) {
    import port._
    def toAutoCompleteJson(implicit messages: Messages): JsObject =
      Json.obj("code" -> code, "displayName" -> messages(displayName), "synonyms" -> portSynonyms)
  }

  implicit class ConversionRatePeriodEnriched(conversion: ConversionRatePeriod) {
    import conversion._
    def display: String = s"$rate ($currencyCode)"
  }

  implicit class PaymentCalculationsEnriched(calculations: PaymentCalculations) {
    import calculations._
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
      TotalCalculationResult(calculations, totalGbpValue, totalTaxDue, totalDutyDue, totalVatDue)

    def toTable(implicit messages: Messages): Table = {
      val tableRows: Seq[Seq[TableRow]] = paymentCalculations.map { tc =>
        val goods = tc.goods.asInstanceOf[ImportGoods]

        Seq(
          TableRow(
            Text(goods.categoryQuantityOfGoods.category)
          ),
          TableRow(
            Text(tc.calculationResult.gbpAmount.formattedInPoundsUI)
          ),
          TableRow(
            Text(tc.calculationResult.duty.formattedInPoundsUI)
          ),
          TableRow(
            Text(
              messages(
                "paymentCalculation.table.col3.row",
                tc.calculationResult.vat.formattedInPoundsUI,
                goods.goodsVatRate.value
              )
            )
          ),
          TableRow(
            Text(tc.calculationResult.taxDue.formattedInPoundsUI)
          )
        )
      } :+ Seq(
        TableRow(
          content = Text(messages("paymentCalculation.table.total")),
          classes = "govuk-table__header",
          colspan = Some(4)
        ),
        TableRow(
          content = Text(totalTaxDue.formattedInPoundsUI),
          classes = "govuk-!-font-weight-bold"
        )
      )

      Table(
        rows = tableRows,
        attributes = Map("style" -> "margin-bottom:60px"),
        head = Some(
          Seq(
            HeadCell(
              Text(messages("paymentCalculation.table.col1.head"))
            ),
            HeadCell(
              Text(messages("paymentCalculation.table.col2.head"))
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
}

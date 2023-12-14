/*
 * Copyright 2023 HM Revenue & Customs
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

import java.text.NumberFormat.getCurrencyInstance
import java.time.{LocalDateTime, ZoneOffset}
import java.util.Locale.UK

import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Table, TableRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.HeadCell
import uk.gov.hmrc.merchandiseinbaggage.config.IsAssistedDigitalConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsVatRates.Zero
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationRequest, CalculationResults}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Country, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.PurchaseDetailsInput

object DataModelEnriched extends IsAssistedDigitalConfiguration {

  implicit class PurchaseDetailsEnriched(details: PurchaseDetails) {
    import details._

    val numericAmount: BigDecimal = BigDecimal(amount)

    def purchaseDetailsInput: PurchaseDetailsInput = PurchaseDetailsInput(amount, currency.code)
  }

  implicit class AmountInPenceEnriched(amountInPence: AmountInPence) {
    import amountInPence._
    val inPounds: BigDecimal                          = (BigDecimal(value) / 100).setScale(2)
    val formattedInPounds: String                     =
      if (value == 0) {
        getCurrencyInstance(UK).format(inPounds)
      } else {
        getCurrencyInstance(UK).format(inPounds).split("\\.00")(0)
      }
    lazy val isPaymentRequired: Boolean               = inPounds.compareTo(BigDecimal(0.0)) > 0
    def fromBigDecimal(in: BigDecimal): AmountInPence = AmountInPence((in * 100).toLong)
  }

  implicit class BigDecimalToAmountInPenceEnriched(bigDecimal: BigDecimal) {
    def fromBigDecimal: AmountInPence = AmountInPence((bigDecimal * 100).toLong)
  }

  implicit class GoodsEnriched(goods: Goods) {
    def calculationRequest(destination: GoodsDestination): CalculationRequest =
      CalculationRequest(goods, destination)
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

  implicit class CalculationResultsEnriched(calculations: CalculationResults) {
    import calculations._
    def totalGbpValue: AmountInPence = AmountInPence(
      calculationResults.map(_.gbpAmount.value).sum
    )

    def totalTaxDue: AmountInPence = AmountInPence(
      calculationResults.map(_.taxDue.value).sum
    )

    def totalDutyDue: AmountInPence = AmountInPence(
      calculationResults.map(_.duty.value).sum
    )

    def totalVatDue: AmountInPence = AmountInPence(
      calculationResults.map(_.vat.value).sum
    )

    def isNothingToPay: Boolean = totalTaxDue.value == 0L

    def isVatOnly: Boolean =
      totalDutyDue.value == 0L &&
        totalVatDue.value != 0L &&
        calculationResults.forall(_.goods.producedInEu == YesNoDontKnow.Yes)

    def isDutyOnly: Boolean =
      totalDutyDue.value != 0L &&
        totalVatDue.value == 0L &&
        calculationResults.forall(_.goods.goodsVatRate == Zero)

    def isDutyAndVat: Boolean = !isVatOnly && !isDutyOnly

    def proofOfOriginNeeded: Boolean =
      calculationResults
        .filter(_.goods.producedInEu == YesNoDontKnow.Yes)
        .map(_.gbpAmount.value)
        .sum > 100000L // £1000 in pence

    def totalCalculationResult: TotalCalculationResult =
      TotalCalculationResult(calculations, totalGbpValue, totalTaxDue, totalDutyDue, totalVatDue)

    def toTable(implicit messages: Messages): Table = {
      val tableRows: Seq[Seq[TableRow]] = calculationResults.map { tc =>
        val goods = tc.goods

        Seq(
          TableRow(
            Text(goods.category)
          ),
          TableRow(
            Text(tc.gbpAmount.formattedInPounds)
          ),
          TableRow(
            Text(tc.duty.formattedInPounds)
          ),
          TableRow(
            Text(
              if (goods.goodsVatRate == Zero) {
                s"${goods.goodsVatRate.value}%"
              } else {
                messages("paymentCalculation.table.col3.row", tc.vat.formattedInPounds, goods.goodsVatRate.value)
              }
            )
          ),
          TableRow(
            Text(tc.taxDue.formattedInPounds)
          )
        )
      } :+ Seq(
        TableRow(
          content = Text(messages("paymentCalculation.table.total")),
          classes = "govuk-table__header",
          colspan = Some(4)
        ),
        TableRow(
          content = Text(totalTaxDue.formattedInPounds),
          classes = "govuk-!-font-weight-bold"
        )
      )

      Table(
        rows = tableRows,
        head = Some(
          Seq(
            HeadCell(
              Text(messages("paymentCalculation.table.col1.head"))
            ),
            HeadCell(
              Text(messages("paymentCalculation.table.col2.head"))
            ),
            HeadCell(
              Text(messages("paymentCalculation.table.col3.head"))
            ),
            HeadCell(
              Text(messages("paymentCalculation.table.col4.head"))
            ),
            HeadCell(
              Text(messages("paymentCalculation.table.col5.head"))
            )
          )
        )
      )
    }
  }

  implicit class DeclarationEnriched(declaration: Declaration) {
    implicit val localDateOrdering: Ordering[LocalDateTime] = Ordering.by(_.toEpochSecond(ZoneOffset.UTC))
    def latestGoods: Seq[Goods]                             =
      if (declaration.amendments.isEmpty) {
        declaration.declarationGoods.goods
      } else {
        declaration.amendments.maxBy(_.dateOfAmendment).goods.goods
      }
  }
}

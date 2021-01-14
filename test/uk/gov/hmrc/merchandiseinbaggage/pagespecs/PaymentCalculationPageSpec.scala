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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs

import com.softwaremill.macwire.wire
import com.softwaremill.quicklens._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, PaymentCalculations}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.PaymentCalculationPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{CustomsAgentPage, PaymentCalculationPage, ReviewGoodsPage}

class PaymentCalculationPageSpec extends BasePageSpec[PaymentCalculationPage] with TaxCalculation {
  override def page: PaymentCalculationPage = wire[PaymentCalculationPage]

  private def setUpTaxCalculationAndOpenPage(
    journey: DeclarationJourney = importJourneyWithTwoCompleteGoodsEntries,
    overThreshold: Option[Int] = Some(0)): PaymentCalculations = {
    val taxCalculation = givenADeclarationWithTaxDue(journey, overThreshold).futureValue

    open(path)

    taxCalculation
  }

  "the Payment Calculation Page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageThatRequiresAtLeastOneCompletedGoodsEntry(path)
    behave like aPageWithABackButton(path, setUpTaxCalculationAndOpenPage(), ReviewGoodsPage.path, shouldGoCya = false)

    "redirect to /goods-over-threshold" when {
      "the total GBP value of the goods exceeds the threshold" in {
        setUpTaxCalculationAndOpenPage(importJourneyWithGoodsOverThreshold, Some(1500000))

        page.readPath() mustBe "/declare-commercial-goods/goods-over-threshold"
      }
    }

    "render" in {
      val taxCalculation = setUpTaxCalculationAndOpenPage(
        importJourneyWithOneCompleteGoodsEntry
          .modify(_.goodsEntries.entries)
          .setTo(Seq(completedGoodsEntry, completedGoodsEntry))
      )
      val paymentCalculations = taxCalculation.paymentCalculations

      page.headerText() mustBe title(taxCalculation.totalTaxDue)
      page.summaryHeaders mustBe Seq("Type of goods", "Value of goods", "Customs", "VAT", "Total")

      Range(0, 1).foreach { index =>
        val calculationResult = paymentCalculations(index).calculationResult
        val goods = paymentCalculations(index).goods

        page.summaryRow(index) mustBe
          Seq(
            goods.categoryQuantityOfGoods.category,
            calculationResult.gbpAmount.formattedInPoundsUI,
            calculationResult.duty.formattedInPoundsUI,
            s"${calculationResult.vat.formattedInPoundsUI} at ${goods.goodsVatRate.value}%",
            calculationResult.taxDue.formattedInPoundsUI
          )
      }

      page.summaryRow(2) mustBe Seq("Payment due", taxCalculation.totalTaxDue.formattedInPoundsUI)
    }

    "redirect to the customs agent page" when {
      "the user clicks the CTA" in {
        setUpTaxCalculationAndOpenPage()
        page.clickOnCTA() mustBe CustomsAgentPage.path
      }
    }
  }
}

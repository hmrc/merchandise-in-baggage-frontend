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

package uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs

import com.softwaremill.macwire.wire
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.PaymentCalculationPage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CustomsAgentPage, PaymentCalculationPage}

class PaymentCalculationPageSpec extends BasePageSpec[PaymentCalculationPage] with TaxCalculation {
  override def page: PaymentCalculationPage = wire[PaymentCalculationPage]

  private def setUpTaxCalculationAndOpenPage() = {
    val taxCalculation = givenADeclarationWithTaxDue(importJourneyWithTwoCompleteGoodsEntries).futureValue

    open(path)

    taxCalculation
  }

  "the Payment Calculation Page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageThatRequiresACompletedGoodsEntry(path)

    "render" in {
      val taxCalculation = setUpTaxCalculationAndOpenPage()
      val paymentCalculations = taxCalculation.paymentCalculations

      page.mustRenderBasicContent(path, title(taxCalculation.totalTaxDue))
      page.summaryHeaders mustBe Seq("Type of goods", "Customs Duty", "VAT", "Total")

      Range(0, 1).foreach { index =>
        val calculationResult = paymentCalculations(index).calculationResult
        val goods = paymentCalculations(index).goods

        page.summaryRow(index) mustBe
          Seq(
            goods.categoryQuantityOfGoods.category,
            calculationResult.duty.formattedInPounds,
            s"${calculationResult.vat.formattedInPounds} at ${goods.goodsVatRate.value}%",
            calculationResult.taxDue.formattedInPounds)
      }

      page.summaryRow(2) mustBe Seq("Payment due", taxCalculation.totalTaxDue.formattedInPounds)
    }

    "redirect to the customs agent page" when {
      "the user clicks the CTA" in {
        setUpTaxCalculationAndOpenPage()
        page.clickOnCTA() mustBe CustomsAgentPage.path
      }
    }
  }
}

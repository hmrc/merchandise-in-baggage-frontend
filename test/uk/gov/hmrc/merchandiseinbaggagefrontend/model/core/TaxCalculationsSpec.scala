package uk.gov.hmrc.merchandiseinbaggagefrontend.model.core

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.calculation.CalculationResult
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpec, CoreTestData}

class TaxCalculationsSpec extends BaseSpec with CoreTestData {

  "Calculate each total tax due" in {
    val calculationResultTwo: CalculationResult = CalculationResult(AmountInPence(20L), AmountInPence(10), AmountInPence(30))
    val calculations = TaxCalculations(Seq(aTaxCalculation, TaxCalculation(aGoods, calculationResultTwo)))

    calculations.totalTaxDue mustBe AmountInPence(10 + 5 + 30 + 7)
    calculations.totalDutyDue mustBe AmountInPence(10 + 5)
    calculations.totalVatDue mustBe AmountInPence(30 + 7)
  }
}

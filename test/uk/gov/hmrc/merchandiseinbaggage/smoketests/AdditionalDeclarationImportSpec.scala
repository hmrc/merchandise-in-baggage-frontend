/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.smoketests

import uk.gov.hmrc.merchandiseinbaggage.model.api.Paid
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.WithinThreshold
import uk.gov.hmrc.merchandiseinbaggage.model.core.{PurchaseDetailsInput, RetrieveDeclaration}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.*
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.*

class AdditionalDeclarationImportSpec extends BaseUiSpec {

  "Additional Declaration Import journey - happy path" should {
    "work as expected" in {

      goto(StartImportPage.path)

      submitPage(NewOrExistingDeclarationPage, "Amend")

      val paidDeclaration = declaration.copy(
        paymentStatus = Some(Paid),
        maybeTotalCalculationResult = Some(aTotalCalculationResult),
        eori = eori,
        mibReference = mibReference
      )

      givenFindByDeclarationReturnSuccess(mibReference, eori, paidDeclaration)

      givenPersistedDeclarationIsFound(paidDeclaration, paidDeclaration.declarationId)

      givenAPaymentCalculation(aCalculationResult)
      givenEoriIsChecked(eori.toString)

      submitPage(RetrieveDeclarationPage, RetrieveDeclaration(mibReference, eori))

      webDriver.getCurrentUrl mustBe fullUrl(PreviousDeclarationDetailsPage.path)

      webDriver.getPageSource must include("wine")
      webDriver.getPageSource must include("99.99, Euro (EUR)")

      submitPage(PreviousDeclarationDetailsPage, "continue")

      submitPage(ExciseAndRestrictedGoodsPage, No)

      submitPage(ValueWeightOfGoodsPage, Yes)

      submitPage(GoodsTypePage, "sock")

      submitPage(PurchaseDetailsPage, PurchaseDetailsInput("100.50", "EUR"))

      submitPage(GoodsOriginPage, "Yes")

      submitPage(GoodsVatRatePage, "Five")

      webDriver.getPageSource must include("sock")
      webDriver.getPageSource must include("Yes")
      webDriver.getPageSource must include("100.50, Euro (EUR)")

      givenAnAmendPaymentCalculations(aCalculationResults.calculationResults, WithinThreshold)
      submitPage(ReviewGoodsPage, "No")

      submitPage(PaymentCalculationPage, "")

      webDriver.getPageSource must include("payButton")
      webDriver.getCurrentUrl mustBe fullUrl(CheckYourAnswersPage.path)
    }
  }
}

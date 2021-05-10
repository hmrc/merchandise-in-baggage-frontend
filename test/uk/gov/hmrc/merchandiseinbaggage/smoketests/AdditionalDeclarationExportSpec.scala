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

package uk.gov.hmrc.merchandiseinbaggage.smoketests

import java.time.LocalDateTime

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.WithinThreshold
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, Paid, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, RetrieveDeclaration}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages._
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._

class AdditionalDeclarationExportSpec extends BaseUiSpec {

  "Additional Declaration Export journey - happy path" should {
    "work as expected" in {
      givenExchangeRateURL("http://something")

      goto(StartExportPage.path)

      submitPage(NewOrExistingDeclarationPage, "Amend")

      val paidDeclaration = declaration.copy(paymentStatus = Some(Paid))

      givenFindByDeclarationReturnSuccess(mibReference, eori, paidDeclaration)

      val sessionId = SessionId()
      val created = LocalDateTime.now
      val id = paidDeclaration.declarationId
      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = sessionId,
          declarationType = DeclarationType.Export,
          maybeEori = Some(eori),
          createdAt = created,
          declarationId = id)

      givenADeclarationJourneyIsPersisted(exportJourney)
      givenDeclarationIsAmendedInBackend
      givenPersistedDeclarationIsFound(exportJourney.declarationIfRequiredAndComplete.get, id)

      submitPage(RetrieveDeclarationPage, RetrieveDeclaration(mibReference, eori))

      webDriver.getPageSource must include("wine")
      webDriver.getPageSource must include("99.99, Euro (EUR)")

      givenAPaymentCalculation(aCalculationResult)
      submitPage(PreviousDeclarationDetailsPage, "continue")

      // controlled or restricted goods
      submitPage(ExciseAndRestrictedGoodsPage, No)

      submitPage(ValueWeightOfGoodsPage, Yes)

      submitPage(GoodsTypePage, "sock")

      submitPage(PurchaseDetailsExportPage, "100.50")

      submitPage(SearchGoodsCountryPage, "FR")

      // Review the goods added
      webDriver.getPageSource must include("sock")
      webDriver.getPageSource must include("France")
      webDriver.getPageSource must include("£100.50")

      givenAnAmendPaymentCalculations(Seq.empty, WithinThreshold)
      submitPage(ReviewGoodsPage, "No")

      webDriver.getPageSource must include("sock")
      webDriver.getPageSource must include("France")

      submitPage(CheckYourAnswersPage, Export)

      webDriver.getCurrentUrl mustBe fullUrl(DeclarationConfirmationPage.path)
    }
  }
}

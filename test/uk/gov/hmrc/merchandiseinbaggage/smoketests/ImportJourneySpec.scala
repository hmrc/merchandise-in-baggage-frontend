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

import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Email, Name}
import uk.gov.hmrc.merchandiseinbaggage.model.core.PurchaseDetailsInput
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages._
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._

class ImportJourneySpec extends BaseUiSpec {

  "Import journey - happy path" should {
    "work as expected" in {
      givenExchangeRateURL("http://something")

      goto(StartImportPage.path)

      submitPage(NewOrExistingDeclarationPage, "New")

      submitPage(GoodsDestinationPage, "GreatBritain")

      submitPage(ExciseAndRestrictedGoodsPage, No)

      submitPage(ValueWeightOfGoodsPage, Yes)

      submitPage(GoodsTypePage, "shoes")

      submitPage(PurchaseDetailsPage, PurchaseDetailsInput("100", "EUR"))

      submitPage(GoodsOriginPage, Yes)

      submitPage(GoodsVatRatePage, "Five")

      givenAPaymentCalculation(aCalculationResult)
      submitPage(ReviewGoodsPage, No)

      submitPage(PaymentCalculationPage, "")

      submitPage(CustomsAgentPage, No)

      givenEoriIsChecked("GB123467800000")
      submitPage(EoriNumberPage, "GB123467800000")

      submitPage(TravellerDetailsPage, Name("firstName", "LastName"))

      submitPage(EnterEmailPage, Email("s@s.s"))

      submitPage(JourneyDetailsPage, "DVR")

      submitPage(GoodsInVehiclePage, Yes)

      submitPage(VehicleSizePage, Yes)

      submitPage(VehicleRegistrationNumberPage, "abc 123")

      webDriver.getCurrentUrl mustBe fullUrl(CheckYourAnswersPage.path)
    }
  }
}

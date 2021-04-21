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

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.WithinThreshold
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CategoryQuantityOfGoods, Email, Name}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages._
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
class ExportJourneySpec extends BaseUiSpec {

  "Export journey" should {
    "work as expected" in {
      goto(StartExportPage.path)

      submitPage(NewOrExistingDeclarationPage, "New")

      submitPage(GoodsDestinationPage, "GreatBritain")

      submitPage(ExciseAndRestrictedGoodsPage, No)

      submitPage(ValueWeightOfGoodsPage, Yes)

      submitPage(GoodsTypeQuantityPage, CategoryQuantityOfGoods("shoes", "one pair"))

      submitPage(PurchaseDetailsExportPage, "100.50")

      submitPage(SearchGoodsCountryPage, "FR")

      submitPage(ReviewGoodsPage, Yes)

      addMoreGoods()

      givenAPaymentCalculation(aCalculationResult, WithinThreshold)
      submitPage(ReviewGoodsPage, No)

      submitPage(CustomsAgentPage, No)

      givenEoriIsChecked("GB123467800000")
      submitPage(EoriNumberPage, "GB123467800000")

      submitPage(TravellerDetailsPage, Name("firstName", "LastName"))

      submitPage(EnterEmailPage, Email("s@s.s"))

      submitPage(JourneyDetailsPage, "DVR")

      submitPage(GoodsInVehiclePage, Yes)

      submitPage(VehicleSizePage, Yes)

      submitPage(VehicleRegistrationNumberPage, "abc 123")

      givenDeclarationIsPersistedInBackend
      givenPersistedDeclarationIsFound()
      submitPage(CheckYourAnswersPage, Export)

      webDriver.getCurrentUrl mustBe fullUrl(DeclarationConfirmationPage.path)

    }
  }

  private def addMoreGoods(): Unit = {
    submitPage(GoodsTypeQuantityPage, CategoryQuantityOfGoods("wine", "one bottle"))
    submitPage(PurchaseDetailsExportPage, "100.50")
    submitPage(SearchGoodsCountryPage, "FR")
  }
}

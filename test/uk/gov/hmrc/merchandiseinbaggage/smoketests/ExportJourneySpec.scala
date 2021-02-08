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

import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages._
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._

class ExportJourneySpec extends BaseUiSpec {

  "Export journey - happy path" should {
    "work as expected" in {
      goto(StartExportPage.path)

      submitPage(GoodsDestinationPage)

      submitPage(ExciseAndRestrictedGoodsPage)

      submitPage(ValueWeightOfGoodsPage)

      submitPage(GoodsTypeQuantityPage)

      submitPage(SearchGoodsCountryPage)

      submitPage(PurchaseDetailsExportPage)

      submitPage(ReviewGoodsPage)

      submitPage(CustomsAgentPage)

      givenEoriIsChecked("GB123467800000")
      submitPage(EoriNumberPage)

      submitPage(TravellerDetailsPage)

      submitPage(EnterEmailPage)

      submitPage(JourneyDetailsPage)

      submitPage(GoodsInVehiclePage)

      givenDeclarationIsPersistedInBackend
      givenPersistedDeclarationIsFound()
      submitPage(CheckYourAnswersPage)

      webDriver.getCurrentUrl mustBe fullUrl(DeclarationConfirmationPage.path)

    }
  }
}

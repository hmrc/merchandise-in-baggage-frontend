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

class ImportJourneySpec extends BaseUiSpec {

  "Import journey - happy path" should {
    "work as expected" in {
      goto(StartImportPage.path)

      submitPage(GoodsDestinationPage)

      submitPage(ExciseAndRestrictedGoodsPage)

      submitPage(ValueWeightOfGoodsPage)

      submitPage(GoodsTypeQuantityPage)

      submitPage(GoodsVatRatePage)

      submitPage(GoodsOriginPage)

      submitPage(PurchaseDetailsPage)

      givenAPaymentCalculation(aCalculationResult)
      submitPage(ReviewGoodsPage)

      submitPage(PaymentCalculationPage)

      submitPage(CustomsAgentPage)

      givenEoriIsChecked("GB123467800000")
      submitPage(EoriNumberPage)

      submitPage(TravellerDetailsPage)

      submitPage(EnterEmailPage)

      submitPage(JourneyDetailsPage)

      submitPage(GoodsInVehiclePage)

    }
  }
}

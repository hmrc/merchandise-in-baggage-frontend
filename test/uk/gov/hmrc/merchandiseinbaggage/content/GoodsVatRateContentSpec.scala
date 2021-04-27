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

package uk.gov.hmrc.merchandiseinbaggage.content

import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.GoodsVatRatePage

class GoodsVatRateContentSpec extends GoodsVatRatePage with CoreTestData {

  "GoodsVatRate" should {
    "display the hint text about type of goods" in {
      givenAJourneyWithSession()
      goToGoodsVatRate(1)

      findByClassName("govuk-inset-text").getText mustBe "Type of goods: wine"
    }
  }
}

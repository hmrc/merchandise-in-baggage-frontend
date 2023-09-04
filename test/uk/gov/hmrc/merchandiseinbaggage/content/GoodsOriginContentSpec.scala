/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.GoodsOriginPage

class GoodsOriginContentSpec extends GoodsOriginPage with CoreTestData {

  "GoodsOrigin" should {
    "display the content as expected" in {
      givenAJourneyWithSession()
      goToGoodsOriginPage(1)

      findByXPath("//label[@for='Yes']").getText mustBe "Yes"
      findById("Yes-item-hint").getText mustBe "Customs Duty is not charged"

      findByXPath("//label[@for='No']").getText mustBe "No"
      findById("No-item-hint").getText mustBe "From 1 Jan 2021, Customs Duty is charged at 3.3%"

      findByXPath("//label[@for='DontKnow']").getText mustBe "I do not know"
      findById("DontKnow-item-hint").getText mustBe "From 1 Jan 2021, Customs Duty is charged at 3.3%"

      findByClassName("govuk-warning-text__text").getText must include(
        "You must carry proof your goods were made in the EU if they have a total value of more than £1,000."
      )

      findByClassName("govuk-details__summary-text").getText mustBe "What you can use as proof"

      findByXPath(
        "//*[@id=\"main-content\"]/div/div/form/div[3]/details/div/p[1]"
      ).getText mustBe "Evidence can be include one of the following:"

      findByXPath(
        "//*[@id=\"main-content\"]/div/div/form/div[3]/details/div/ul/li[1]"
      ).getText mustBe "packaging or a label attached to the item showing that it was produced or made in the EU"
      findByXPath(
        "//*[@id=\"main-content\"]/div/div/form/div[3]/details/div/ul/li[2]"
      ).getText mustBe "evidence the item is handmade or homegrown in the EU"
      findByXPath(
        "//*[@id=\"main-content\"]/div/div/form/div[3]/details/div/ul/li[3]"
      ).getText mustBe "documents given to you by the seller that show the item was produced or made in the EU"
      findByXPath(
        "//*[@id=\"main-content\"]/div/div/form/div[3]/details/div/ul/li[4]"
      ).getText mustBe "a ‘statement on origin’ from the supplier of the item"

      findByXPath(
        "//*[@id=\"main-content\"]/div/div/form/div[3]/details/div/p[2]"
      ).getText mustBe "If you do not have the appropriate evidence you will have to pay customs duty on this item."

      findByClassName("govuk-button").getText mustBe "Continue"
    }
  }
}

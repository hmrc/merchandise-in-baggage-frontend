/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.GoodsOverThresholdPage
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.GoodsOverThresholdPage._
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._

class GoodsOverThresholdContentSpec extends GoodsOverThresholdPage with CoreTestData {

  declarationTypes.foreach { (importOrExport: DeclarationType) =>
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenAJourneyWithSession(declarationType = importOrExport)

        givenAPaymentCalculation(aCalculationResult)

        goToGoodsOverThresholdPage()

        pageTitle must startWith(title)

        elementText(findByTagName("h1")) mustBe messages(
          "goodsOverThreshold.GreatBritain.heading",
          thresholdValueInUI
        )

        findByXPath("""//*[@id="main-content"]/div/div/p[1]""").getText mustBe messages(
          s"goodsOverThreshold.GreatBritain.$importOrExport.p1",
          thresholdValueInUI
        )

        findByXPath("""//*[@id="main-content"]/div/div/p[2]""").getText must startWith(
          messages("goodsOverThreshold.p2")
        )
        findByXPath("""//*[@id="main-content"]/div/div/p[2]/a""").getText mustBe messages(
          s"goodsOverThreshold.p2.$importOrExport.a.text"
        )
        findByXPath("""//*[@id="main-content"]/div/div/p[2]/a""").getAttribute("href") mustBe messages(
          s"goodsOverThreshold.p2.$importOrExport.a.href"
        )
        findByXPath("""//*[@id="main-content"]/div/div/p[2]/a""").getText mustBe messages(
          s"goodsOverThreshold.p2.$importOrExport.a.text"
        )

        if (importOrExport == Import) {
          findByXPath("""//*[@id="main-content"]/div/div/p[8]""").getText   must startWith(
            messages(s"goodsOverThreshold.p8.1")
          )
          findByXPath("""//*[@id="main-content"]/div/div/p[8]/a""").getAttribute(
            "href"
          ) mustBe "https://www.gov.uk/government/collections/exchange-rates-for-customs-and-vat"
          findByXPath("""//*[@id="main-content"]/div/div/p[8]/a""").getText must startWith(
            messages("goodsOverThreshold.p8.a.text")
          )
        }
      }
    }
  }
}

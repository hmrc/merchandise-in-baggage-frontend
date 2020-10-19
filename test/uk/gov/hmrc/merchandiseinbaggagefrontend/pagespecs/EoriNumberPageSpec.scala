/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs

import uk.gov.hmrc.merchandiseinbaggagefrontend.CoreTestData
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, YesNo}

class EoriNumberPageSpec extends BasePageSpec with CoreTestData {

  private def createDeclarationJourney(declarationJourney: DeclarationJourney): Unit = {
    testOnlyDeclarationJourneyPage.open()
    testOnlyDeclarationJourneyPage.fillOutForm(declarationJourney)
    testOnlyDeclarationJourneyPage.clickOnSubmitButton()
  }

  "/enter-eori-number" should {
    "render correctly for agent" in {
      createDeclarationJourney(completedDeclarationJourney)

      eoriNumberPage.open()
      eoriNumberPage.mustRenderBasicContent()
    }

    "render correctly for trader" in {
      createDeclarationJourney(completedDeclarationJourney.copy(maybeIsACustomsAgent = Some(YesNo.No)))

      eoriNumberPage.open()
      eoriNumberPage.mustRenderBasicContent("What is your EORI number?")
    }

    "allow the user to enter their eori number and redirect to /traveller-details" in {
      createDeclarationJourney(completedDeclarationJourney)

      eoriNumberPage.open()
      eoriNumberPage.fillOutForm("GB123467800000")
      eoriNumberPage.clickOnSubmitButton()
    }
  }
}

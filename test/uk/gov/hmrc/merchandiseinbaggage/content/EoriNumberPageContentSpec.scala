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
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo._
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.EoriNumberPage

class EoriNumberPageContentSpec extends EoriNumberPage with CoreTestData {

  "page" should {
    "show content specific to agents if the user is an agent" in {
      givenAJourneyWithSession()
      goToEoriPage(Yes, completedDeclarationJourney.declarationType)

      findById("eori-hint").getText mustBe "Your client will need an EORI number that starts with GB to use this service. For example, GB123467800000."
      findByXPath("//*[@id=\"main-content\"]/div/div/form/p").getText must include("Your client can")
      findByXPath("//*[@id=\"main-content\"]/div/div/form/p").getText must include("apply for an EORI number")
      findByXPath("//*[@id=\"main-content\"]/div/div/form/p").getText must include("(usually takes less than 10 minutes)")
    }

    "show content specific to trader if the user is a trader" in {
      val updatedJourney = completedDeclarationJourney.copy(maybeIsACustomsAgent = Some(No))
      givenAJourneyWithSession(declarationJourney = updatedJourney)
      goToEoriPage(No, updatedJourney.declarationType)

      findById("eori-hint").getText mustBe "You need an EORI number that starts with GB to use this service. For example, GB123467800000."
      findByXPath("//*[@id=\"main-content\"]/div/div/form/p").getText must include("Apply for an EORI number")
      findByXPath("//*[@id=\"main-content\"]/div/div/form/p").getText must include("(usually takes less than 10 minutes)")
    }
  }
}

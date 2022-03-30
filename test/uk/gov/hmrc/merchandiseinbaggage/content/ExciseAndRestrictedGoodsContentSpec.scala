/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.ExciseAndRestrictedGoodsPage

class ExciseAndRestrictedGoodsContentSpec extends ExciseAndRestrictedGoodsPage with CoreTestData {

  "Back links" should {
    "link to /check-your-answers when a New journey is complete" in {
      givenAJourneyWithSession()
      goToExciseAndRestrictedPage(Import)

      findByClassName("govuk-back-link").getAttribute("href") mustBe fullUrl("/declare-commercial-goods/check-your-answers")
    }

    "link to /goods-destination when a New journey is not complete" in {
      givenAJourneyWithSession(declarationJourney = startedImportJourney)
      goToExciseAndRestrictedPage(Import)

      findByClassName("govuk-back-link").getAttribute("href") mustBe fullUrl("/declare-commercial-goods/goods-destination")
    }

    "link to /check-your-answers when an Amend journey is complete" in {
      givenAJourneyWithSession(journeyType = Amend, declarationType = Export, declarationJourney = completeAmendExportJourney)
      goToExciseAndRestrictedPage(Export)

      findByClassName("govuk-back-link").getAttribute("href") mustBe fullUrl("/declare-commercial-goods/check-your-answers")
    }

    "link to /previous-declaration-details when an Amend journey is not complete" in {
      givenAJourneyWithSession(journeyType = Amend, declarationType = Export, declarationJourney = startedAmendExportJourney)
      goToExciseAndRestrictedPage(Export)

      findByClassName("govuk-back-link").getAttribute("href") mustBe fullUrl("/declare-commercial-goods/previous-declaration-details")
    }
  }
}

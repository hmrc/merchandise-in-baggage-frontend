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

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CannotUseServicePage, ExciseAndRestrictedGoodsPage, GoodsDestinationPage}

import scala.concurrent.ExecutionContext.Implicits.global

class ExciseAndRestrictedGoodsPageSpec extends BasePageSpec[ExciseAndRestrictedGoodsPage] {
  override lazy val page: ExciseAndRestrictedGoodsPage = exciseAndRestrictedGoodsPage

  "the excise and restricted goods page" should {
    behave like aPageWithSimpleRendering(givenAnImportJourneyIsStarted())
    behave like aPageWhichRequiresADeclarationJourney()

    "render correctly" when {
      "a declaration has been completed" in {
        val exciseAndRestrictedGoods = completedDeclarationJourney.maybeExciseOrRestrictedGoods.get
        createDeclarationJourney(completedDeclarationJourney)

        page.open()
        page.previouslyEnteredValuesAreDisplayed(exciseAndRestrictedGoods)
      }
    }

    s"update the persisted declaration journey and redirect to ${GoodsDestinationPage.path}" when {
      "the user answers 'No'" in {
        createDeclarationJourney()

        page.open()
        page.fillOutForm(No)
        page.clickOnCTA() mustBe GoodsDestinationPage.path

        ensurePersistedDetailsMatch(No)
      }
    }

    s"update the persisted declaration journey and redirect to ${CannotUseServicePage.path}" when {
      "the user answers 'Yes'" in {
        createDeclarationJourney()

        page.open()
        page.fillOutForm(Yes)
        page.clickOnCTA() mustBe CannotUseServicePage.path

        ensurePersistedDetailsMatch(Yes)
      }
    }
  }

  private def ensurePersistedDetailsMatch(exciseAndRestrictedGoods: YesNo) = {
    val persistedJourneys = declarationJourneyRepository.findAll().futureValue
    persistedJourneys.size mustBe 1
    persistedJourneys.head.maybeExciseOrRestrictedGoods mustBe Some(exciseAndRestrictedGoods)
  }
}

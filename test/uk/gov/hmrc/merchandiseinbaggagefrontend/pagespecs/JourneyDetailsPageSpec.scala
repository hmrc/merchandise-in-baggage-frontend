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

import java.time.LocalDate

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.Port
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.Ports.{Dover, Heathrow}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.JourneyDetailsPage

import scala.concurrent.ExecutionContext.Implicits.global

class JourneyDetailsPageSpec extends BasePageSpec[JourneyDetailsPage] {
  override lazy val page: JourneyDetailsPage = journeyDetailsPage

  private val today = LocalDate.now
  private val tomorrow = today.plusDays(1)

  "/journey-details" should {
    behave like aPageWithSimpleRendering(givenAnImportJourneyIsStarted())
    behave like aPageWhichRequiresADeclarationJourney()

    "render correctly" when {
      "a declaration has been completed" in {
        val journeyDetailsEntry = completedDeclarationJourney.maybeJourneyDetailsEntry.get
        createDeclarationJourney(completedDeclarationJourney)

        page.open()
        page.mustRenderBasicContent()
        page.previouslyEnteredValuesAreDisplayed(journeyDetailsEntry.placeOfArrival, journeyDetailsEntry.dateOfArrival)
      }
    }

    "redirect to /check-your-answers for a foot passenger port" when {
      "the declaration is complete" in {
        createDeclarationJourney()

        page.open()
        page.fillOutForm(Heathrow, tomorrow)
        page.clickOnSubmitButtonMustRedirectTo("/merchandise-in-baggage/check-your-answers")

        ensurePersistedDetailsMatch(Heathrow, tomorrow)
      }
    }

    "redirect to /goods-in-vehicle for a vehicle port" in {
      givenAnImportJourneyIsStarted()

      page.open()
      page.fillOutForm(Dover, tomorrow)
      page.clickOnSubmitButtonMustRedirectTo("/merchandise-in-baggage/goods-in-vehicle")

      ensurePersistedDetailsMatch(Dover, tomorrow)
    }

    "redirect to /invalid-request" when {
      "when a declaration for a foot passenger port is not complete" in {
        givenAnImportJourneyIsStarted()

        page.open()
        page.fillOutForm(Heathrow, today)
        page.clickOnSubmitButtonMustRedirectTo("/merchandise-in-baggage/invalid-request")
      }
    }
  }

  private def ensurePersistedDetailsMatch(placeOfArrival: Port, dateOfArrival: LocalDate) = {
    val persistedJourneys = declarationJourneyRepository.findAll().futureValue
    persistedJourneys.size mustBe 1
    persistedJourneys.head.maybeJourneyDetailsEntry.get.placeOfArrival mustBe placeOfArrival
    persistedJourneys.head.maybeJourneyDetailsEntry.get.dateOfArrival mustBe dateOfArrival
  }
}

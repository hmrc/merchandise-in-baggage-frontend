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

import scala.concurrent.ExecutionContext.Implicits.global

class JourneyDetailsPageSpec extends BasePageSpec {
  private val today = LocalDate.now
  private val tomorrow = today.plusDays(1)

  "/journey-details" should {
    "render correctly" when {
      "a declaration has been started" in {
        startImportJourney()

        journeyDetailsPage.open()
        journeyDetailsPage.mustRenderBasicContent()
      }

      "a declaration has been completed" in {
        val journeyDetailsEntry = completedDeclarationJourney.maybeJourneyDetailsEntry.get
        createDeclarationJourney(completedDeclarationJourney)

        journeyDetailsPage.open()
        journeyDetailsPage.mustRenderBasicContent()
        journeyDetailsPage.previouslyEnteredValuesAreDisplayed(journeyDetailsEntry.placeOfArrival, journeyDetailsEntry.dateOfArrival)
      }
    }

    "redirect to /check-your-answers for a foot passenger port" when {
      "the declaration is complete" in {
        createDeclarationJourney()

        journeyDetailsPage.open()
        journeyDetailsPage.fillOutForm(Heathrow, tomorrow)
        journeyDetailsPage.clickOnSubmitButtonMustRedirectTo("/merchandise-in-baggage/check-your-answers")

        ensureJourneyEntryDetailsMatch(Heathrow, tomorrow)
      }
    }

    "redirect to /goods-in-vehicle for a vehicle port" in {
      startImportJourney()

      journeyDetailsPage.open()
      journeyDetailsPage.fillOutForm(Dover, tomorrow)
      journeyDetailsPage.clickOnSubmitButtonMustRedirectTo("/merchandise-in-baggage/goods-in-vehicle")

      ensureJourneyEntryDetailsMatch(Dover, tomorrow)
    }

    "redirect to /invalid-request" when {
      "the declaration has not been started" in {
        journeyDetailsPage.open()
        journeyDetailsPage.redirectsToInvalidRequest()
      }

      "when a declaration for a foot passenger port is not complete" in {
        startImportJourney()

        journeyDetailsPage.open()
        journeyDetailsPage.fillOutForm(Heathrow, today)
        journeyDetailsPage.clickOnSubmitButtonMustRedirectTo("/merchandise-in-baggage/invalid-request")
      }
    }
  }

  private def ensureJourneyEntryDetailsMatch(placeOfArrival: Port, dateOfArrival: LocalDate) = {
    val persistedJourneys = declarationJourneyRepository.findAll().futureValue
    persistedJourneys.size mustBe 1
    persistedJourneys.head.maybeJourneyDetailsEntry.get.placeOfArrival mustBe placeOfArrival
    persistedJourneys.head.maybeJourneyDetailsEntry.get.dateOfArrival mustBe dateOfArrival
  }
}

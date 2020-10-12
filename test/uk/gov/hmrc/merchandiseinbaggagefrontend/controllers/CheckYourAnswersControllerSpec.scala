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

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._

class CheckYourAnswersControllerSpec extends DeclarationJourneyControllerSpec {
  private lazy val controller = app.injector.instanceOf[CheckYourAnswersController]

  private val url: String = routes.CheckYourAnswersController.onPageLoad().url
  private val getRequestWithSessionId = buildGet(url, sessionId)

  "onPageLoad" should {
    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "render the page" when {
      "a declaration has been completed" in {
        givenADeclarationJourneyIsPersisted(completedDeclarationJourney)

        val eventualResponse = controller.onPageLoad(getRequestWithSessionId)
        val content = contentAsString(eventualResponse)

        status(eventualResponse) mustBe OK

        content must include("Check your answers before making your declaration")

        content must include("Details of the goods")
        content must include("Type of goods")
        content must include("wine")
        content must include("cheese")
        content must include("Country")
        content must include("France")
        content must include("Price")
        content must include("99.99, Eurozone Euro (EUR)")
        content must include("199.99, Eurozone Euro (EUR)")
        content must include("Tax due")
        content must include("£30.11")

        content must include("Personal details")
        content must include("Name")
        content must include("Terry Test")
        content must include("EORI number")
        content must include("TerrysEori")

        content must include("Journey details")
        content must include("Place of arrival")
        content must include("Dover")
        content must include("Date of arrival")
        content must include(completedDeclarationJourney.maybeJourneyDetails.get.formattedDateOfArrival)
        content must include("Travelling by vehicle")
        content must include("Yes")
        content must include("Vehicle registration number")
        content must include("T5 RRY")

        content must include("Now send your declaration")
        content must include("I understand that:")
        content must include("I must pay Customs Duty and VAT on goods I bring into the UK for trade or business use.")
        content must include("I will need to show my declaration and invoices if I am stopped by Border Force.")
        content must include("Warning")
        content must include("If you do not declare all your goods before entering the UK you may be fined a penalty and have your goods detained by Border Force.")
        content must include("Accept and Pay")
      }
    }

    "redirect to /invalid-request" when {
      "the journey is not complete" in {
        givenADeclarationJourneyIsPersisted(incompleteDeclarationJourney)

        val result = controller.onPageLoad()(buildGet(url))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.InvalidRequestController.onPageLoad().url
      }
    }
  }
}

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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{Eori, YesNo}
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.EoriNumberView

import scala.concurrent.ExecutionContext.Implicits.global

class EoriNumberControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val controller =
    new EoriNumberController(
      controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[EoriNumberView])

  "onPageLoad" must {
    val url = routes.EoriNumberController.onPageLoad().url
    val getRequest = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "redirect to /invalid-request" when {
      "a declaration has been started but required answer has not been provided" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney)

        val result = controller.onPageLoad()(getRequest)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustBe routes.InvalidRequestController.onPageLoad().url
      }
    }

    "return OK and render the view" when {
      "a declaration has been started and user is a trader" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(maybeIsACustomsAgent = Some(YesNo.No)))

        val result = controller.onPageLoad()(getRequest)
        val content = contentAsString(result)

        status(result) mustEqual OK
        content must include("What is your EORI number?")
      }

      "a declaration has been started and user is an agent" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(maybeIsACustomsAgent = Some(YesNo.Yes)))

        val result = controller.onPageLoad()(getRequest)
        val content = contentAsString(result)

        status(result) mustEqual OK
        content must include("What is the EORI number of the company importing the goods?")
      }

      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(
          maybeIsACustomsAgent = Some(YesNo.No),
          maybeEori = Some(Eori("GB123467800000"))
        ))

        val result = controller.onPageLoad()(getRequest)
        val content = contentAsString(result)

        status(result) mustEqual OK
        content must include("GB123467800000")
      }
    }
  }

  "onSubmit" must {
    val url = routes.EoriNumberController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /traveller-details" when {
      "a declaration is started and a valid selection submitted" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(
          maybeIsACustomsAgent = Some(YesNo.No)
        ))

        val request = postRequest.withFormUrlEncodedBody(("eori", "GB123467800000"))

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.TravellerDetailsController.onPageLoad().url

        startedImportJourney.maybeEori mustBe None
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.maybeEori mustBe Some(Eori("GB123467800000"))
      }
    }

    "return BAD_REQUEST and errors" when {
      "nothing is entered" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(
          maybeIsACustomsAgent = Some(YesNo.No)
        ))

        val result = controller.onSubmit()(postRequest)
        val content = contentAsString(result)

        status(result) mustEqual BAD_REQUEST
        content must include("Enter an EORI number")
      }

      "invalid value entered" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(
          maybeIsACustomsAgent = Some(YesNo.No)
        ))

        val request = postRequest.withFormUrlEncodedBody(("eori", "invalid"))

        val result = controller.onSubmit()(request)
        val content = contentAsString(result)

        status(result) mustEqual BAD_REQUEST
        content must include("Enter a valid EORI number")
      }
    }
  }
}

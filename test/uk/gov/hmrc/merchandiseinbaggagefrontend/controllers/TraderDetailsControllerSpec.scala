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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.TraderDetailsForm.{firstName, form, lastName}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.Name
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.TraderDetailsPage

import scala.concurrent.ExecutionContext.Implicits.global

class TraderDetailsControllerSpec extends DeclarationJourneyControllerSpec {
  private val name = Name("Terry", "Test")

  private lazy val controller =
    new TraderDetailsController(
      controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[TraderDetailsPage])

  "onPageLoad" must {
    val url = routes.TraderDetailsController.onPageLoad().url
    val getRequest = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "return OK and render the view" when {
      "a declaration has been started" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad()(getRequest)
        val content = contentAsString(result)

        status(result) mustEqual OK

        content must include("Enter the name of the person carrying the goods")
        content must include("First name")
        content must include("Last name")
        content must include("Continue")
      }
    }

    "return OK and render the view" when {
      "a declaration has been started and trader name persisted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(maybeName = Some(name)))

        val result = controller.onPageLoad()(getRequest)
        val content = contentAsString(result)

        status(result) mustEqual OK

        content must include("Enter the name of the person carrying the goods")
        content must include("First name")
        content must include(name.firstName)
        content must include("Last name")
        content must include(name.lastName)
        content must include("Continue")
      }
    }
  }

  "onSubmit" must {
    val url = routes.GoodsDestinationController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Persist the submitted details and redirect to /enter-trader-address" when {
      "a declaration is started and a valid selection submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val request = postRequest.withFormUrlEncodedBody((firstName, name.firstName), (lastName, name.lastName))

        form.bindFromRequest()(request)

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SkeletonJourneyController.enterTraderAddress().toString
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.maybeName mustBe Some(name)
      }
    }

    "return BAD_REQUEST and errors" when {
      "no entry is made" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        form.bindFromRequest()(postRequest)

        val result = controller.onSubmit()(postRequest)
        val content = contentAsString(result)

        status(result) mustEqual BAD_REQUEST

        content must include("Enter the name of the person carrying the goods")
        content must include("First name")
        content must include("Last name")
        content must include("Continue")
        content must include("There is a problem")
        content must include("This field is required")
      }
    }
  }
}

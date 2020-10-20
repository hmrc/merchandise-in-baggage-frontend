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

import play.api.mvc.{Call, Result}
import play.api.test.Helpers._

import scala.concurrent.Future

class SkeletonJourneyControllerSpec extends DeclarationJourneyControllerSpec {
  private lazy val controller = app.injector.instanceOf[SkeletonJourneyController]

  "journeyDetails" should {
    val url = routes.SkeletonJourneyController.journeyDetails().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Journey Details"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.journeyDetails(getRequest), title, routes.GoodsInVehicleController.onPageLoad())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.journeyDetails(buildGet(url)))
      }
    }
  }

  "vehicleSize" should {
    val url = routes.SkeletonJourneyController.vehicleSize().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Are you bringing the goods in a small vehicle?"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.vehicleSize(getRequest), title, routes.SkeletonJourneyController.vehicleRegistrationNumber())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.vehicleSize(buildGet(url)))
      }
    }
  }

  "vehicleRegistrationNumber" should {
    val url = routes.SkeletonJourneyController.vehicleRegistrationNumber().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Enter the vehicle registration number"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.vehicleRegistrationNumber(getRequest), title, routes.CheckYourAnswersController.onPageLoad())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.vehicleRegistrationNumber(buildGet(url)))
      }
    }
  }

  private def ensure(eventualResponse: Future[Result], title: String, call: Call) = {
    val content = contentAsString(eventualResponse)

    status(eventualResponse) mustBe OK
    content must include(title)
    content must include(call.url)
  }

  private def ensureRedirectToInvalidRequestPage(eventualResponse: Future[Result]) = {
    status(eventualResponse) mustBe 303
    redirectLocation(eventualResponse).get mustBe routes.InvalidRequestController.onPageLoad().url
  }
}

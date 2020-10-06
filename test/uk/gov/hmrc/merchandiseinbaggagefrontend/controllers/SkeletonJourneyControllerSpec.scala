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

  "purchaseDetails" should {
    val url = routes.SkeletonJourneyController.purchaseDetails().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "How much did you pay for the x?"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.purchaseDetails(getRequest), title, routes.SkeletonJourneyController.invoiceNumber())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.purchaseDetails(buildGet(url)))
      }
    }
  }

  "invoiceNumber" should {
    val url = routes.SkeletonJourneyController.invoiceNumber().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "What is the invoice number for the x?"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.invoiceNumber(getRequest), title, routes.ReviewGoodsController.onPageLoad())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.invoiceNumber(buildGet(url)))
      }
    }
  }

  "removeGoods" should {
    val url = routes.SkeletonJourneyController.removeGoods().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Are you sure you want to remove the x?"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.removeGoods(getRequest), title, routes.SkeletonJourneyController.goodsRemoved())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.removeGoods(buildGet(url)))
      }
    }
  }

  "goodsRemoved" should {
    val url = routes.SkeletonJourneyController.goodsRemoved().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "You removed your goods"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.goodsRemoved(getRequest), title, routes.SearchGoodsController.onPageLoad())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.goodsRemoved(buildGet(url)))
      }
    }
  }

  "taxCalculation" should {
    val url = routes.SkeletonJourneyController.taxCalculation().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Tax due on these goods £x"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.taxCalculation(getRequest), title, routes.SkeletonJourneyController.customsAgent())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.taxCalculation(buildGet(url)))
      }
    }
  }

  "customsAgent" should {
    val url = routes.SkeletonJourneyController.customsAgent().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Are you a customs agent?"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.customsAgent(getRequest), title, routes.SkeletonJourneyController.agentDetails())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.customsAgent(buildGet(url)))
      }
    }
  }

  "agentDetails" should {
    val url = routes.SkeletonJourneyController.agentDetails().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Enter the business name of the customs agent"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.agentDetails(getRequest), title, routes.SkeletonJourneyController.enterAgentAddress())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.agentDetails(buildGet(url)))
      }
    }
  }

  "enterAgentAddress" should {
    val url = routes.SkeletonJourneyController.enterAgentAddress().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Find your address"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.enterAgentAddress(getRequest), title, routes.SkeletonJourneyController.selectAgentAddress())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.enterAgentAddress(buildGet(url)))
      }
    }
  }

  "selectAgentAddress" should {
    val url = routes.SkeletonJourneyController.selectAgentAddress().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Select your address"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.selectAgentAddress(getRequest), title, routes.SkeletonJourneyController.enterEoriNumber())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.selectAgentAddress(buildGet(url)))
      }
    }
  }

  "enterEoriNumber" should {
    val url = routes.SkeletonJourneyController.enterEoriNumber().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "What is your EORI number?"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.enterEoriNumber(getRequest), title, routes.TravellerDetailsController.onPageLoad())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.enterEoriNumber(buildGet(url)))
      }
    }
  }

  "journeyDetails" should {
    val url = routes.SkeletonJourneyController.journeyDetails().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Journey Details"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.journeyDetails(getRequest), title, routes.SkeletonJourneyController.goodsInVehicle())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.journeyDetails(buildGet(url)))
      }
    }
  }

  "goodsInVehicle" should {
    val url = routes.SkeletonJourneyController.goodsInVehicle().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Are you travelling by vehicle?"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.goodsInVehicle(getRequest), title, routes.SkeletonJourneyController.vehicleSize())
      }
    }

    "redirect to /invalid-request" when {
      "a declaration journey has not been started" in {
        ensureRedirectToInvalidRequestPage(controller.goodsInVehicle(buildGet(url)))
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

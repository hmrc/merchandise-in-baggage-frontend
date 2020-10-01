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

  "selectDeclarationType" should {
    val url = routes.SkeletonJourneyController.selectDeclarationType().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "What do you need to declare?"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.selectDeclarationType(getRequest), title, routes.ExciseAndRestrictedGoodsController.onPageLoad())
      }
    }

    "redirect to /start" when {
      "a declaration journey has not been started" in {
        ensureRedirectToStart(controller.selectDeclarationType(buildGet(url)))
      }
    }
  }

  "searchGoods" should {
    val url = routes.SkeletonJourneyController.searchGoods().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "What goods are you bringing into the UK?"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.searchGoods(getRequest), title, routes.SearchGoodsCountryController.onPageLoad())
      }
    }

    "redirect to /start" when {
      "a declaration journey has not been started" in {
        ensureRedirectToStart(controller.searchGoods(buildGet(url)))
      }
    }
  }

  "purchaseDetails" should {
    val url = routes.SkeletonJourneyController.purchaseDetails().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "How much did you pay for the x?"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.purchaseDetails(getRequest), title, routes.ReviewGoodsController.onPageLoad())
      }
    }

    "redirect to /start" when {
      "a declaration journey has not been started" in {
        ensureRedirectToStart(controller.purchaseDetails(buildGet(url)))
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

    "redirect to /start" when {
      "a declaration journey has not been started" in {
        ensureRedirectToStart(controller.taxCalculation(buildGet(url)))
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
        ensure(controller.customsAgent(getRequest), title, routes.TraderDetailsController.onPageLoad())
      }
    }

    "redirect to /start" when {
      "a declaration journey has not been started" in {
        ensureRedirectToStart(controller.customsAgent(buildGet(url)))
      }
    }
  }

  "enterTraderAddress" should {
    val url = routes.SkeletonJourneyController.enterTraderAddress().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Find your address"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.enterTraderAddress(getRequest), title, routes.SkeletonJourneyController.selectTraderAddress())
      }
    }

    "redirect to /start" when {
      "a declaration journey has not been started" in {
        ensureRedirectToStart(controller.enterTraderAddress(buildGet(url)))
      }
    }
  }

  "selectTraderAddress" should {
    val url = routes.SkeletonJourneyController.selectTraderAddress().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Select your address"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.selectTraderAddress(getRequest), title, routes.SkeletonJourneyController.enterEoriNumber())
      }
    }

    "redirect to /start" when {
      "a declaration journey has not been started" in {
        ensureRedirectToStart(controller.selectTraderAddress(buildGet(url)))
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
        ensure(controller.enterEoriNumber(getRequest), title, routes.SkeletonJourneyController.traderJourneyDetails())
      }
    }

    "redirect to /start" when {
      "a declaration journey has not been started" in {
        ensureRedirectToStart(controller.enterEoriNumber(buildGet(url)))
      }
    }
  }

  "traderJourneyDetails" should {
    val url = routes.SkeletonJourneyController.traderJourneyDetails().url

    "render the page" when {
      "a declaration journey has been started" in {
        val title = "Journey Details"
        val getRequest = buildGet(url, sessionId)

        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        ensure(controller.traderJourneyDetails(getRequest), title, routes.CheckYourAnswersController.onPageLoad())
      }
    }

    "redirect to /start" when {
      "a declaration journey has not been started" in {
        ensureRedirectToStart(controller.traderJourneyDetails(buildGet(url)))
      }
    }
  }

  private def ensure(eventualResponse: Future[Result], title: String, call: Call) = {
    val content = contentAsString(eventualResponse)

    status(eventualResponse) mustBe OK
    content must include(title)
    content must include(call.url)
  }

  private def ensureRedirectToStart(eventualResponse: Future[Result]) = {
    status(eventualResponse) mustBe 303
    redirectLocation(eventualResponse).get mustBe routes.StartController.onPageLoad().url
  }
}

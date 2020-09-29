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
    "render the page" in {
      val title = "What do you need to declare?"
      val getRequest = buildGet(routes.SkeletonJourneyController.selectDeclarationType().url)

      ensure(controller.selectDeclarationType(getRequest), title, routes.ExciseAndRestrictedGoodsController.onPageLoad())
    }
  }

  "searchGoods" should {
    "render the page" in {
      val title = "What goods are you bringing into the UK?"
      val getRequest = buildGet(routes.SkeletonJourneyController.searchGoods().url)

      ensure(controller.searchGoods(getRequest), title, routes.SkeletonJourneyController.searchGoodsCountry())
    }
  }

  "searchGoodsCountry" should {
    "render the page" in {
      val title = "In what country did you buy the x?"
      val getRequest = buildGet(routes.SkeletonJourneyController.searchGoodsCountry().url)

      ensure(controller.searchGoodsCountry(getRequest), title, routes.SkeletonJourneyController.purchaseDetails())
    }
  }

  "purchaseDetails" should {
    "render the page" in {
      val title = "How much did you pay for the x?"
      val getRequest = buildGet(routes.SkeletonJourneyController.purchaseDetails().url)

      ensure(controller.purchaseDetails(getRequest), title, routes.SkeletonJourneyController.reviewGoods())
    }
  }

  "reviewGoods" should {
    "render the page" in {
      val title = "Review your goods"
      val getRequest = buildGet(routes.SkeletonJourneyController.reviewGoods().url)

      ensure(controller.reviewGoods(getRequest), title, routes.SkeletonJourneyController.taxCalculation())
    }
  }

  "taxCalculation" should {
    "render the page" in {
      val title = "Tax due on these goods £x"
      val getRequest = buildGet(routes.SkeletonJourneyController.taxCalculation().url)

      ensure(controller.taxCalculation(getRequest), title, routes.SkeletonJourneyController.customsAgent())
    }
  }

  "customsAgent" should {
    "render the page" in {
      val title = "Are you a customs agent?"
      val getRequest = buildGet(routes.SkeletonJourneyController.customsAgent().url)

      ensure(controller.customsAgent(getRequest), title, routes.SkeletonJourneyController.traderDetails())
    }
  }

  "traderDetails" should {
    "render the page" in {
      val title = "Enter the name of the person carrying the goods"
      val getRequest = buildGet(routes.SkeletonJourneyController.traderDetails().url)

      ensure(controller.traderDetails(getRequest), title, routes.SkeletonJourneyController.enterTraderAddress())
    }
  }

  "enterTraderAddress" should {
    "render the page" in {
      val title = "Find your address"
      val getRequest = buildGet(routes.SkeletonJourneyController.enterTraderAddress().url)

      ensure(controller.enterTraderAddress(getRequest), title, routes.SkeletonJourneyController.selectTraderAddress())
    }
  }

  "selectTraderAddress" should {
    "render the page" in {
      val title = "Select your address"
      val getRequest = buildGet(routes.SkeletonJourneyController.selectTraderAddress().url)

      ensure(controller.selectTraderAddress(getRequest), title, routes.SkeletonJourneyController.enterEoriNumber())
    }
  }

  "enterEoriNumber" should {
    "render the page" in {
      val title = "What is your EORI number?"
      val getRequest = buildGet(routes.SkeletonJourneyController.enterEoriNumber().url)

      ensure(controller.enterEoriNumber(getRequest), title, routes.SkeletonJourneyController.traderJourneyDetails())
    }
  }

  "traderJourneyDetails" should {
    "render the page" in {
      val title = "Journey Details"
      val getRequest = buildGet(routes.SkeletonJourneyController.traderJourneyDetails().url)

      ensure(controller.traderJourneyDetails(getRequest), title, routes.CheckYourAnswersController.onPageLoad())
    }
  }

  private def ensure(eventualResponse: Future[Result], title: String, call: Call) = {
    val content = contentAsString(eventualResponse)

    status(eventualResponse) mustBe OK
    content must include(title)
    content must include(call.url)
  }
}

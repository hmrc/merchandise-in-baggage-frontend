/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpecWithApplication

import scala.concurrent.Future

class SkeletonJourneyControllerSpec extends BaseSpecWithApplication {
  private lazy val controller = app.injector.instanceOf[SkeletonJourneyController]

  "start" should {
    "render the page" in {
      val title = "Declaring merchandise in your baggage"
      val getRequest = buildGet(routes.SkeletonJourneyController.start().url)

      ensure(controller.start(getRequest), title, routes.SkeletonJourneyController.selectDeclarationType())
    }
  }

  "selectDeclarationType" should {
    "render the page" in {
      val title = "What do you need to declare?"
      val getRequest = buildGet(routes.SkeletonJourneyController.selectDeclarationType().url)

      ensure(controller.selectDeclarationType(getRequest), title, routes.SkeletonJourneyController.exciseAndRestrictedGoods())
    }
  }

  "exciseAndRestrictedGoods" should {
    "render the page" in {
      val title = "Are you bringing in excise goods or restricted goods?"
      val getRequest = buildGet(routes.SkeletonJourneyController.exciseAndRestrictedGoods().url)

      ensure(controller.exciseAndRestrictedGoods(getRequest), title, routes.GoodsDestinationController.onPageLoad())
    }
  }

  "valueWeightOfGoods" should {
    "render the page" in {
      val title = "Is the total value of the goods over £1500 or 1000kg?"
      val getRequest = buildGet(routes.SkeletonJourneyController.valueWeightOfGoods().url)

      ensure(controller.valueWeightOfGoods(getRequest), title, routes.SkeletonJourneyController.searchGoods())
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

    status(eventualResponse) mustBe 200
    content must include(title)
    content must include(call.url)
  }
}

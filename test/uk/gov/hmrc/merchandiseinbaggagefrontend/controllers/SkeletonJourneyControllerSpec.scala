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

      ensure(controller.start(getRequest), title, routes.SkeletonJourneyController.importExport())
    }
  }

  "importExport" should {
    "render the page" in {
      val title = "What do you need to declare?"
      val getRequest = buildGet(routes.SkeletonJourneyController.importExport().url)

      ensure(controller.importExport(getRequest), title, routes.SkeletonJourneyController.multipleCountriesEuCheck())
    }
  }

  "multipleCountriesEuCheck" should {
    "render the page" in {
      val title = "Where are the goods coming from?"
      val getRequest = buildGet(routes.SkeletonJourneyController.multipleCountriesEuCheck().url)

      ensure(controller.multipleCountriesEuCheck(getRequest), title, routes.SkeletonJourneyController.goodsExcise())
    }
  }

  "goodsExcise" should {
    "render the page" in {
      val title = "Are you bringing in excise or restricted goods?"
      val getRequest = buildGet(routes.SkeletonJourneyController.goodsExcise().url)

      ensure(controller.goodsExcise(getRequest), title, routes.SkeletonJourneyController.valueWeightOfGoods())
    }
  }

  "valueWeightOfGoods" should {
    "render the page" in {
      val title = "Is the total value of the goods over £873 or 1000kg?"
      val getRequest = buildGet(routes.SkeletonJourneyController.valueWeightOfGoods().url)

      ensure(controller.valueWeightOfGoods(getRequest), title, routes.SkeletonJourneyController.goodsType())
    }
  }

  "goodsType" should {
    "render the page" in {
      val title = "Enter the detail of each individual item"
      val getRequest = buildGet(routes.SkeletonJourneyController.goodsType().url)

      ensure(controller.goodsType(getRequest), title, routes.SkeletonJourneyController.goodsCategory())
    }
  }

  "goodsCategory" should {
    "render the page" in {
      val title = "Which rate of VAT do the goods fit into?"
      val getRequest = buildGet(routes.SkeletonJourneyController.goodsCategory().url)

      ensure(controller.goodsCategory(getRequest), title, routes.SkeletonJourneyController.goodsDetailsWhere())
    }
  }

  "goodsDetailsWhere" should {
    "render the page" in {
      val title = "In which country did you purchase the goods?"
      val getRequest = buildGet(routes.SkeletonJourneyController.goodsDetailsWhere().url)

      ensure(controller.goodsDetailsWhere(getRequest), title, routes.SkeletonJourneyController.goodsDetailsCost())
    }
  }

  "goodsDetailsCost" should {
    "render the page" in {
      val title = "How much did you pay for the goods?"
      val getRequest = buildGet(routes.SkeletonJourneyController.goodsDetailsCost().url)

      ensure(controller.goodsDetailsCost(getRequest), title, routes.SkeletonJourneyController.goodsReview())
    }
  }

  "goodsReview" should {
    "render the page" in {
      val title = "Review your goods"
      val getRequest = buildGet(routes.SkeletonJourneyController.goodsReview().url)

      ensure(controller.goodsReview(getRequest), title, routes.SkeletonJourneyController.calculation())
    }
  }

  "calculation" should {
    "render the page" in {
      val title = "Tax due on these goods"
      val getRequest = buildGet(routes.SkeletonJourneyController.calculation().url)

      ensure(controller.calculation(getRequest), title, routes.SkeletonJourneyController.traderAgent())
    }
  }

  "traderAgent" should {
    "render the page" in {
      val title = "Are you a customs agent?"
      val getRequest = buildGet(routes.SkeletonJourneyController.traderAgent().url)

      ensure(controller.traderAgent(getRequest), title, routes.SkeletonJourneyController.traderDetails())
    }
  }

  "traderDetails" should {
    "render the page" in {
      val title = "What is the name of carrying the goods?"
      val getRequest = buildGet(routes.SkeletonJourneyController.traderDetails().url)

      ensure(controller.traderDetails(getRequest), title, routes.SkeletonJourneyController.traderAddress())
    }
  }

  "traderAddress" should {
    "render the page" in {
      val title = "What is your address?"
      val getRequest = buildGet(routes.SkeletonJourneyController.traderAddress().url)

      ensure(controller.traderAddress(getRequest), title, routes.SkeletonJourneyController.traderAddressList())
    }
  }

  "traderAddressList" should {
    "render the page" in {
      val title = "Select your address"
      val getRequest = buildGet(routes.SkeletonJourneyController.traderAddressList().url)

      ensure(controller.traderAddressList(getRequest), title, routes.SkeletonJourneyController.traderEori())
    }
  }

  "traderEori" should {
    "render the page" in {
      val title = "What is your EORI number?"
      val getRequest = buildGet(routes.SkeletonJourneyController.traderEori().url)

      ensure(controller.traderEori(getRequest), title, routes.SkeletonJourneyController.traderJourney())
    }
  }

  "traderJourney" should {
    "render the page" in {
      val title = "What your journey details?"
      val getRequest = buildGet(routes.SkeletonJourneyController.traderJourney().url)

      ensure(controller.traderJourney(getRequest), title, routes.CheckYourAnswersController.onPageLoad())
    }
  }

  private def ensure(eventualResponse: Future[Result], title: String, call: Call) = {
    val content = contentAsString(eventualResponse)

    status(eventualResponse) mustBe 200
    content must include(title)
    content must include(call.url)
  }
}

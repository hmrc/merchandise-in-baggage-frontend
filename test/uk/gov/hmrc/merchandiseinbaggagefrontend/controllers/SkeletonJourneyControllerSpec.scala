/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpecWithApplication

class SkeletonJourneyControllerSpec extends BaseSpecWithApplication {
  private lazy val controller = app.injector.instanceOf[SkeletonJourneyController]

  "traderEori" should {
    "render the page" in {
      val getRequest = buildGet(routes.SkeletonJourneyController.traderEori().url)

      val eventualResponse = controller.traderEori(getRequest)
      status(eventualResponse) mustBe 200
      contentAsString(eventualResponse) must include("What is your EORI number?")
    }
  }

  "traderJourney" should {
    "render the page" in {
      val getRequest = buildGet(routes.SkeletonJourneyController.traderJourney().url)

      val eventualResponse = controller.traderJourney(getRequest)
      status(eventualResponse) mustBe 200
      contentAsString(eventualResponse) must include("What your journey details?")
    }
  }
}

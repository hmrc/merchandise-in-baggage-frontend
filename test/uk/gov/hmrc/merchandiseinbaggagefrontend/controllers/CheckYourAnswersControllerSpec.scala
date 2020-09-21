/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpecWithApplication

class CheckYourAnswersControllerSpec extends BaseSpecWithApplication {
  private lazy val controller = app.injector.instanceOf[CheckYourAnswersController]

  "checkYourAnswers" should {
    "render the page" in {
      val getRequest = buildGet(routes.CheckYourAnswersController.onPageLoad().url)

      val eventualResponse = controller.onPageLoad(getRequest)
      status(eventualResponse) mustBe 200
      contentAsString(eventualResponse) must include("Check your answers before making your declaration")
    }
  }
}

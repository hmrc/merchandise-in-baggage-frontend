/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.StartView

class StartControllerSpec extends BaseSpecWithApplication {

  private lazy val view = injector.instanceOf[StartView]
  private lazy val controller = new StartController(controllerComponents, view)

  "StartController" must {

    "return OK and correct view for GET" in {
      val getRequest = buildGet(routes.StartController.onPageLoad().url)
      val result = controller.onPageLoad()(getRequest)

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(getRequest, messagesApi.preferred(getRequest), appConfig).toString
    }
  }

}

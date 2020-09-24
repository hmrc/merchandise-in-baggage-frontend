/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.CannotUseServiceView

class CannotUseServiceControllerSpec extends BaseSpecWithApplication {
  "onPageLoad" must {
    "return OK and render the view" in {
      val view = injector.instanceOf[CannotUseServiceView]
      val controller = new CannotUseServiceController(controllerComponents, view)
      val request = buildGet(routes.CannotUseServiceController.onPageLoad().url)
      val result = controller.onPageLoad()(request)

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(request, messagesApi.preferred(request), appConfig).toString
    }
  }
}

/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.GoodsDestinationFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.GoodsDestinationView

class GoodsDestinationControllerSpec extends BaseSpecWithApplication {

  lazy val route: String = routes.GoodsDestinationController.onPageLoad().url

  val formProvider = new GoodsDestinationFormProvider()
  val form = formProvider()

  val getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, route).withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  val view = injector.instanceOf[GoodsDestinationView]

  val controller = new GoodsDestinationController(controllerComponents, formProvider, view)

  "GoodsDestinationController" must {

    "return OK and correct view for GET" in {
      val result = controller.onPageLoad()(getRequest)

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(form)(getRequest, messagesApi.preferred(getRequest), appConfig).toString
    }

    //TODO assert against redirection/storage when MIBM-77 done
    "return OK when valid selection submitted" in {
      val request = FakeRequest(POST, route).withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        .withFormUrlEncodedBody(("value", "ni"))

      val submittedForm = form.bindFromRequest()(request)

      val result = controller.onSubmit()(request)

      status(result) mustEqual OK
    }

    "return BAD_REQUEST and errors when no selection made" in {
      val request = FakeRequest(POST, route).withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

      val submittedForm = form.bindFromRequest()(request)

      val result = controller.onSubmit()(request)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(submittedForm)(request, messagesApi.preferred(request), appConfig).toString
    }
  }
}

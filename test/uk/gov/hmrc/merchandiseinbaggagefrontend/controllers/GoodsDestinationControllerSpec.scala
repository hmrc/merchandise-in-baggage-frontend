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

  private lazy val route = routes.GoodsDestinationController.onPageLoad().url

  private val formProvider = new GoodsDestinationFormProvider()
  private val form = formProvider()

  private val getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, route).withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  private lazy val view = injector.instanceOf[GoodsDestinationView]

  private lazy val controller = new GoodsDestinationController(controllerComponents, formProvider, view)

  "GoodsDestinationController" must {

    "return OK and correct view for GET" in {
      val result = controller.onPageLoad()(getRequest)

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(form)(getRequest, messagesApi.preferred(getRequest), appConfig).toString
    }

    //TODO assert against redirection/storage when MIBM-77 done
    "Redirect to /value-weight-of-goods when valid selection submitted" in {
      val request = FakeRequest(POST, route).withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        .withFormUrlEncodedBody(("value", "ni"))

      form.bindFromRequest()(request)

      val result = controller.onSubmit()(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).get mustEqual routes.SkeletonJourneyController.valueWeightOfGoods().toString
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

/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.ExciseAndRestrictedGoodsFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ExciseAndRestrictedGoodsView

class ExciseAndRestrictedGoodsControllerSpec extends BaseSpecWithApplication {
  private val formProvider = new ExciseAndRestrictedGoodsFormProvider()
  private val form = formProvider()

  private lazy val controller = new ExciseAndRestrictedGoodsController(controllerComponents, formProvider, view)
  private lazy val view = injector.instanceOf[ExciseAndRestrictedGoodsView]

  "onPageLoad" must {
    "return OK and render the view" in {
      val request = buildGet(routes.ExciseAndRestrictedGoodsController.onPageLoad().url)
      val result = controller.onPageLoad()(request)

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(form)(request, messagesApi.preferred(request), appConfig).toString
    }
  }

  "onSubmit" must {
    val postRequest = buildPost(routes.ExciseAndRestrictedGoodsController.onSubmit().url)

    "Redirect to /goods-destination" when {
      "false is submitted" in {
        val request = postRequest.withFormUrlEncodedBody(("value", "false"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.GoodsDestinationController.onPageLoad().toString
      }
    }

    "Redirect to /cannot-use-service" when {
      "true is submitted" in {
        val request = postRequest.withFormUrlEncodedBody(("value", "true"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.CannotUseServiceController.onPageLoad().toString
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        val submittedForm = form.bindFromRequest()(postRequest)
        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(submittedForm)(postRequest, messagesApi.preferred(postRequest), appConfig).toString
      }
    }
  }
}

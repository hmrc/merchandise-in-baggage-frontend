/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.GoodsDestinationFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.GoodsDestinationView

class GoodsDestinationControllerSpec extends BaseSpecWithApplication {
  private val formProvider = new GoodsDestinationFormProvider()
  private val form = formProvider()

  private lazy val view = injector.instanceOf[GoodsDestinationView]
  private lazy val controller = new GoodsDestinationController(controllerComponents, formProvider, view)

  "onPageLoad" must {
    "return OK and render the view" in {
      val getRequest = buildGet(routes.GoodsDestinationController.onPageLoad().url)
      val result = controller.onPageLoad()(getRequest)

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(form)(getRequest, messagesApi.preferred(getRequest), appConfig).toString
    }
  }

  "onSubmit" must {
    val postRequest = buildPost(routes.GoodsDestinationController.onSubmit().url)

    //TODO assert against redirection/storage when MIBM-77 done
    "Redirect to /value-weight-of-goods" when {
      "a valid selection submitted" in {
        val request = postRequest.withFormUrlEncodedBody(("value", "ni"))

        form.bindFromRequest()(request)

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SkeletonJourneyController.valueWeightOfGoods().toString
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

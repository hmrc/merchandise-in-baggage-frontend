/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.ExciseAndRestrictedGoodsFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ExciseAndRestrictedGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class ExciseAndRestrictedGoodsControllerSpec extends DeclarationJourneyControllerSpec {
  private val formProvider = new ExciseAndRestrictedGoodsFormProvider()
  private val form = formProvider()

  private lazy val view = injector.instanceOf[ExciseAndRestrictedGoodsView]

  private lazy val controller =
    new ExciseAndRestrictedGoodsController(
      controllerComponents, actionBuilder, formProvider, declarationJourneyRepository, view)

  "onPageLoad" must {
    val url = routes.ExciseAndRestrictedGoodsController.onPageLoad().url
    val request = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "return OK and render the view" when {
      "a declaration has been started" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form)(request, messagesApi.preferred(request), appConfig).toString
      }
    }

    "return OK and render the view" when {
      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(maybeExciseOrRestrictedGoods = Some(true)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(true))(request, messagesApi.preferred(request), appConfig).toString
      }
    }
  }

  "onSubmit" must {
    val url = routes.ExciseAndRestrictedGoodsController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /goods-destination" when {
      "a declaration is started and false is submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val request = postRequest.withFormUrlEncodedBody(("value", "false"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.GoodsDestinationController.onPageLoad().toString
      }
    }

    "Redirect to /cannot-use-service" when {
      "a declaration is started and true is submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val request = postRequest.withFormUrlEncodedBody(("value", "true"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.CannotUseServiceController.onPageLoad().toString
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val submittedForm = form.bindFromRequest()(postRequest)
        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(submittedForm)(postRequest, messagesApi.preferred(postRequest), appConfig).toString
      }
    }
  }
}

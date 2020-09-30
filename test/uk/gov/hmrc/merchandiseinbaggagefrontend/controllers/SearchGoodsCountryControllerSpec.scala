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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.SearchGoodsCountryFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.SearchGoodsCountryView

import scala.concurrent.ExecutionContext.Implicits.global

class SearchGoodsCountryControllerSpec extends DeclarationJourneyControllerSpec {
  private val formProvider = new SearchGoodsCountryFormProvider()
  private val form = formProvider(appConfig.autocompleteCountries)

  private lazy val view = injector.instanceOf[SearchGoodsCountryView]
  private lazy val controller =
    new SearchGoodsCountryController(
      controllerComponents, actionBuilder, formProvider, declarationJourneyRepository, view)

  "onPageLoad" must {
    val url = routes.SearchGoodsCountryController.onPageLoad().url
    val getRequest = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "return OK and render the view" when {
      "a declaration has been started" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad()(getRequest)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form)(getRequest, messagesApi.preferred(getRequest), appConfig).toString
      }
    }

    "return OK and render the view" when {
      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(maybeSearchGoodsCountry = Some(appConfig.autocompleteCountries.head)))

        val result = controller.onPageLoad()(getRequest)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(appConfig.autocompleteCountries.head))(getRequest, messagesApi.preferred(getRequest), appConfig).toString
      }
    }
  }

  "onSubmit" must {
    val url = routes.SearchGoodsCountryController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /value-weight-of-goods" when {
      "a declaration is started and a valid selection submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val request = postRequest.withFormUrlEncodedBody(("value", "Austria"))

        form.bindFromRequest()(request)

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SkeletonJourneyController.purchaseDetails().toString
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

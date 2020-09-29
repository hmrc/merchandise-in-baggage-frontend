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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.ValueWeightOfGoodsFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestination._
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ValueWeightOfGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class ValueWeightOfGoodsControllerSpec extends DeclarationJourneyControllerSpec {
  private val formProvider = new ValueWeightOfGoodsFormProvider()
  private val form = formProvider()

  private lazy val view = injector.instanceOf[ValueWeightOfGoodsView]

  private lazy val controller =
    new ValueWeightOfGoodsController(
      controllerComponents, actionBuilder, formProvider, declarationJourneyRepository, view)

  "onPageLoad" must {
    val url = routes.ValueWeightOfGoodsController.onPageLoad().url
    val request = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "redirect to /goods-destination" when {
      "a declaration has been started but a required answer is missing in the journey" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.GoodsDestinationController.onPageLoad().toString
      }
    }

    "return OK and render the view" when {
      "a declaration has been started with a destination of Northern Ireland" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(maybeGoodsDestination = Some(NorthernIreland)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NorthernIreland)(request, messagesApi.preferred(request), appConfig).toString
      }

      "a declaration has been started with a destination of England, Wales or Scotland" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(maybeGoodsDestination = Some(EngScoWal)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, EngScoWal)(request, messagesApi.preferred(request), appConfig).toString
      }

      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney
          .copy(
            maybeGoodsDestination = Some(NorthernIreland),
            maybeValueWeightOfGoods = Some(true)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(true), NorthernIreland)(request, messagesApi.preferred(request), appConfig).toString
      }
    }
  }

  "onSubmit" must {
    val url = routes.ValueWeightOfGoodsController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /search-goods" when {
      "a declaration is started and false is submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(maybeGoodsDestination = Some(NorthernIreland)))

        val request = postRequest.withFormUrlEncodedBody(("value", "false"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SkeletonJourneyController.searchGoods().toString

        startedDeclarationJourney.maybeValueWeightOfGoods mustBe None
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.maybeValueWeightOfGoods mustBe Some(false)
      }
    }

    "Redirect to /cannot-use-service" when {
      "a declaration is started and true is submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(maybeGoodsDestination = Some(NorthernIreland)))

        val request = postRequest.withFormUrlEncodedBody(("value", "true"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.CannotUseServiceController.onPageLoad().toString
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(maybeGoodsDestination = Some(NorthernIreland)))

        val submittedForm = form.bindFromRequest()(postRequest)
        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(submittedForm, NorthernIreland)(postRequest, messagesApi.preferred(postRequest), appConfig).toString
      }
    }
  }
}

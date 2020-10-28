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

import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo._
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ValueWeightOfGoodsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValueWeightOfGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val controller =
    new ValueWeightOfGoodsController(
      controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[ValueWeightOfGoodsView])

  private def ensureContent(result: Future[Result]) = {
    val content = contentAsString(result)

    content must include("Is the total value of the goods over")
    content must include("kilograms (kg)?")
    content must include("Continue")

    content
  }

  "onPageLoad" must {
    val url = routes.ValueWeightOfGoodsController.onPageLoad().url
    val request = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "redirect to /goods-destination" when {
      "a declaration has been started but a required answer is missing in the journey" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney)

        val result = controller.onPageLoad()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.GoodsDestinationController.onPageLoad().toString
      }
    }

    "return OK and render the view" when {
      "a declaration has been started with a destination of Northern Ireland" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(maybeGoodsDestination = Some(NorthernIreland)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        ensureContent(result) must include("£873 or 1000")
      }

      "a declaration has been started with a destination of England, Wales or Scotland" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(maybeGoodsDestination = Some(GreatBritain)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        ensureContent(result) must include("£1500 or 1000")
      }

      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney
          .copy(
            maybeGoodsDestination = Some(NorthernIreland),
            maybeValueWeightOfGoodsExceedsThreshold = Some(Yes)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        ensureContent(result)
      }
    }
  }

  "onSubmit" must {
    val url = routes.ValueWeightOfGoodsController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /search-goods" when {
      "a declaration is started and No is submitted" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(maybeGoodsDestination = Some(NorthernIreland)))

        val request = postRequest.withFormUrlEncodedBody(("value", "No"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SearchGoodsController.onPageLoad(1).toString

        startedImportJourney.maybeValueWeightOfGoodsExceedsThreshold mustBe None
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.maybeValueWeightOfGoodsExceedsThreshold mustBe Some(No)
      }
    }

    "Redirect to /cannot-use-service" when {
      "a declaration is started and Yes is submitted" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(maybeGoodsDestination = Some(NorthernIreland)))

        val request = postRequest.withFormUrlEncodedBody(("value", "Yes"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.CannotUseServiceController.onPageLoad().toString
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(maybeGoodsDestination = Some(NorthernIreland)))

        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        ensureContent(result) must include("Select one of the options below")
      }
    }
  }
}

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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestinations
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.GoodsDestinationView

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsDestinationControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val controller =
    new GoodsDestinationController(
      controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[GoodsDestinationView])

  "onPageLoad" must {
    val url = routes.GoodsDestinationController.onPageLoad().url
    val getRequest = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "return OK and render the view" when {
      "a declaration has been started" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney)

        val result = controller.onPageLoad()(getRequest)

        status(result) mustEqual OK
      }
    }

    "return OK and render the view" when {
      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(maybeGoodsDestination = Some(GoodsDestinations.values.head)))

        val result = controller.onPageLoad()(getRequest)

        status(result) mustEqual OK
      }
    }
  }

  "onSubmit" must {
    val url = routes.GoodsDestinationController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to the correct page" when {
      "a declaration is started and NorthernIreland submitted" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney)

        val request = postRequest.withFormUrlEncodedBody(("value", NorthernIreland.toString))

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.GoodsRouteDestinationController.onPageLoad().toString
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.maybeGoodsDestination mustBe Some(NorthernIreland)
      }

      "a declaration is started and GreatBritain submitted" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney)

        val request = postRequest.withFormUrlEncodedBody(("value", GreatBritain.toString))

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.ExciseAndRestrictedGoodsController.onPageLoad().toString
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.maybeGoodsDestination mustBe Some(GreatBritain)
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney)

        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}

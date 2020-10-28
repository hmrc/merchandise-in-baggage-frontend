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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo._
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ExciseAndRestrictedGoodsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExciseAndRestrictedGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val controller =
    new ExciseAndRestrictedGoodsController(
      controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[ExciseAndRestrictedGoodsView])

  private def ensureContent(result: Future[Result]) = {
    val content = contentAsString(result)

    content must include("Are you bringing in excise goods or restricted goods?")
    content must include("Excise goods are alcohol, tobacco, or fuel. You can")
    content must include("check if your goods are restricted (opens in new tab or window).")
    content must include("https://www.gov.uk/government/publications/restricted-goods-for-merchandise-in-baggage")
    content must include("Continue")

    content
  }

  "onPageLoad" must {
    val url = routes.ExciseAndRestrictedGoodsController.onPageLoad().url
    val request = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "return OK and render the view" when {
      "a declaration has been started" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney)

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        ensureContent(result)
      }
    }

    "return OK and render the view" when {
      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(maybeExciseOrRestrictedGoods = Some(Yes)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        ensureContent(result)
      }
    }
  }

  "onSubmit" must {
    val url = routes.ExciseAndRestrictedGoodsController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /goods-destination" when {
      "a declaration is started and No is submitted" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney)

        val request = postRequest.withFormUrlEncodedBody(("value", "No"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.ValueWeightOfGoodsController.onPageLoad().toString

        startedImportJourney.maybeExciseOrRestrictedGoods mustBe None
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.maybeExciseOrRestrictedGoods mustBe Some(No)
      }
    }

    "Redirect to /cannot-use-service" when {
      "a declaration is started and Yes is submitted" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney)

        val request = postRequest.withFormUrlEncodedBody(("value", "Yes"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.CannotUseServiceController.onPageLoad().toString
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney)

        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        ensureContent(result) must include("Select one of the options below")
      }
    }
  }
}

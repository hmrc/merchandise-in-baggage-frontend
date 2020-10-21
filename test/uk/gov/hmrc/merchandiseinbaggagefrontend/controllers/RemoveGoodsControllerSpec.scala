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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{GoodsEntries, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.RemoveGoodsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RemoveGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val controller =
    new RemoveGoodsController(
      controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[RemoveGoodsView])

  private def ensureContent(result: Future[Result], goodsEntry: GoodsEntry) = {
    val content = contentAsString(result)

    content must include(s"Are you sure you want to remove the ${goodsEntry.maybeCategoryQuantityOfGoods.get.category}?")
    content must include("Confirm")

    content
  }

  "onPageLoad" must {
    val url = routes.RemoveGoodsController.onPageLoad(1).url
    val request = buildGet(url, sessionId)

    behave like anIndexedEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "redirect to /invalid-request" when {
      "a declaration has been started but a required answer is missing in the journey" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad(1)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.InvalidRequestController.onPageLoad().toString
      }
    }

    "return OK and render the view" when {
      "a declaration has been started" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = GoodsEntries(completedGoodsEntry)))

        val result = controller.onPageLoad(1)(request)

        status(result) mustEqual OK
        ensureContent(result, completedGoodsEntry)
      }
    }
  }

  "onSubmit" must {
    val url = routes.RemoveGoodsController.onSubmit(1).url
    val postRequest = buildPost(url, sessionId)

    behave like anIndexedEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /review-goods" when {
      "a declaration is started and No is submitted" in {
        val before = startedDeclarationJourney.copy(goodsEntries = GoodsEntries(Seq(completedGoodsEntry, completedGoodsEntry)))
        givenADeclarationJourneyIsPersisted(before)

        val request = postRequest.withFormUrlEncodedBody(("value", "No"))
        val result = controller.onSubmit(1)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.ReviewGoodsController.onPageLoad().toString

        //Assert than no deletions were made
        before.goodsEntries.entries.size mustBe 2
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.goodsEntries.entries.size mustBe 2
      }
    }

    "Redirect to /review-goods" when {
      "a declaration is started and Yes is submitted and goods still exist in the journey" in {
        val before = startedDeclarationJourney.copy(goodsEntries = GoodsEntries(Seq(completedGoodsEntry, completedGoodsEntry)))
        givenADeclarationJourneyIsPersisted(before)

        val request = postRequest.withFormUrlEncodedBody(("value", "Yes"))
        val result = controller.onSubmit(1)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.ReviewGoodsController.onPageLoad().toString

        //Assert than entry was removed
        before.goodsEntries.entries.size mustBe 2
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.goodsEntries.entries.size mustBe 1
      }
    }

    "Redirect to /goods-removed" when {
      "a declaration is started and Yes is submitted and no more goods exist in the journey" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = GoodsEntries(Seq(completedGoodsEntry))))

        val request = postRequest.withFormUrlEncodedBody(("value", "Yes"))
        val result = controller.onSubmit(1)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.GoodsRemovedController.onPageLoad().toString
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = GoodsEntries(Seq(completedGoodsEntry))))

        val result = controller.onSubmit(1)(postRequest)

        status(result) mustEqual BAD_REQUEST
        ensureContent(result, completedGoodsEntry) must include("Select one of the options below")
      }
    }
  }
}

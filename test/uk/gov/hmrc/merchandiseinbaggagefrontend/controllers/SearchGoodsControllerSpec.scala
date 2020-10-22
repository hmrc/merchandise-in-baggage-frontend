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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{CategoryQuantityOfGoods, GoodsEntries, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.SearchGoodsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SearchGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val controller =
    new SearchGoodsController(
      controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[SearchGoodsView])

  private def ensureContent(result: Future[Result]) = {
    val content = contentAsString(result)

    content must include("What type of goods are you bringing into the UK?")
    content must include("Add your goods by their type or category. For example, clothes, electronics, or food.")
    content must include("Continue")

    content
  }

  private val goodsEntries = GoodsEntries(completedGoodsEntry)

  "onPageLoad" must {
    val url = routes.SearchGoodsController.onPageLoad(1, change = false).url
    val getRequest = buildGet(url, sessionId)

    behave like anIndexedEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "return OK and render the view" when {
      "a declaration has been started" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad(1, change = false)(getRequest)

        status(result) mustEqual OK
        ensureContent(result)
      }
    }

    "return OK and render the view" when {
      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = goodsEntries))

        val result = controller.onPageLoad(1, change = false)(getRequest)

        status(result) mustEqual OK
        ensureContent(result)
      }
    }
  }

  "onSubmit" must {
    val url = routes.SearchGoodsController.onSubmit(1, change = false).url
    val postRequest = buildPost(url, sessionId)

    behave like anIndexedEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "redirect to /goods-vat-rate" when {
      "a declaration is started and a valid selection submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val request = postRequest.withFormUrlEncodedBody(("category", "test category"), ("quantity", "100"))

        val result = controller.onSubmit(1, change = false)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.GoodsVatRateController.onPageLoad(1, change = false).toString

        startedDeclarationJourney.goodsEntries mustBe GoodsEntries.empty
        declarationJourneyRepository
          .findBySessionId(sessionId)
          .futureValue
          .get
          .goodsEntries
          .entries
          .head mustBe GoodsEntry(Some(CategoryQuantityOfGoods("test category", "100")))
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onSubmit(1, change = false)(postRequest)

        status(result) mustEqual BAD_REQUEST
        ensureContent(result) must include("Enter the type of goods")
      }
    }
  }
}

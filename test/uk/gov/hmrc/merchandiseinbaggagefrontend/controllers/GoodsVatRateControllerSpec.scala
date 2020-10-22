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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsVatRates.Twenty
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{GoodsEntries, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.GoodsVatRateView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GoodsVatRateControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val controller =
    new GoodsVatRateController(
      controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[GoodsVatRateView])

  private def ensureContent(result: Future[Result], goodsEntry: GoodsEntry) = {
    val content = contentAsString(result)

    content must include(s"Check which VAT rate applies to the ${goodsEntry.maybeCategoryQuantityOfGoods.get.category}")
    content must include("The Customs Duty on most goods has a standard rate of 20% VAT applied to it, but some goods such as food and children’s clothes have a lower rate.")
    content must include("Continue")

    content
  }

  "onPageLoad" must {
    val url = routes.GoodsVatRateController.onPageLoad(1, change = false).url
    val getRequest = buildGet(url, sessionId)

    behave like anIndexedEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "return OK and render the view" when {
      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = GoodsEntries(completedGoodsEntry)))

        val result = controller.onPageLoad(1, change = false)(getRequest)

        status(result) mustEqual OK
        ensureContent(result, completedGoodsEntry)
      }
    }
  }

  "onSubmit" must {
    val url = routes.GoodsVatRateController.onSubmit(1, change = false).url
    val postRequest = buildPost(url, sessionId)

    behave like anIndexedEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /search-goods-country" when {
      "a declaration is started and a valid selection submitted" in {
        givenADeclarationJourneyIsPersisted(declarationJourneyWithStartedGoodsEntry)

        val request = postRequest.withFormUrlEncodedBody(("value", "Twenty"))

        val result = controller.onSubmit(1, change = false)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SearchGoodsCountryController.onPageLoad(1, change = false).toString

        startedDeclarationJourney.goodsEntries.entries.head mustBe GoodsEntry.empty
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.goodsEntries.entries.head.maybeGoodsVatRate mustBe Some(Twenty)
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(declarationJourneyWithStartedGoodsEntry)

        val result = controller.onSubmit(1, change = false)(postRequest)

        status(result) mustEqual BAD_REQUEST
        ensureContent(result, startedGoodsEntry) must include("Select one of the options below")
      }
    }
  }
}

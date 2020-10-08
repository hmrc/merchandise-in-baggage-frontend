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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.GoodsVatRateFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{CategoryQuantityOfGoods, GoodsEntries, GoodsEntry, GoodsVatRate}
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.GoodsVatRateView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GoodsVatRateControllerSpec extends DeclarationJourneyControllerSpec {
  private val formProvider = new GoodsVatRateFormProvider()
  private val form = formProvider()

  private lazy val controller =
    new GoodsVatRateController(
      controllerComponents, actionBuilder, formProvider, declarationJourneyRepository, injector.instanceOf[GoodsVatRateView])

  private def ensureContent(result: Future[Result], goodsEntry: GoodsEntry) = {
    val content = contentAsString(result)

    content must include(s"Check which VAT rate applies to the ${goodsEntry.categoryQuantityOfGoods.category}")
    content must include("The Customs Duty on most goods has a standard rate of 20% VAT applied to it, but some goods such as food and children’s clothes have a lower rate.")
    content must include("Continue")

    content
  }

  "onPageLoad" must {
    val url = routes.GoodsVatRateController.onPageLoad().url
    val getRequest = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "redirect to /invalid-request" when {
      "a declaration has been started but a required answer is missing in the journey" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad()(getRequest)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.InvalidRequestController.onPageLoad().toString
      }
    }

    "return OK and render the view" when {
      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = GoodsEntries(completedGoodsEntry)))

        val result = controller.onPageLoad()(getRequest)

        status(result) mustEqual OK
        ensureContent(result, completedGoodsEntry)
      }
    }
  }

  "onSubmit" must {
    val url = routes.GoodsVatRateController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /search-goods-country" when {
      "a declaration is started and a valid selection submitted" in {
        val before =
          startedDeclarationJourney.copy(goodsEntries = GoodsEntries(GoodsEntry(CategoryQuantityOfGoods("test good", "123"))))

        givenADeclarationJourneyIsPersisted(before)

        val request = postRequest.withFormUrlEncodedBody(("value", "twenty"))

        form.bindFromRequest()(request)

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SearchGoodsCountryController.onPageLoad().toString

        before.goodsEntries.entries.head mustBe GoodsEntry(CategoryQuantityOfGoods("test good", "123"))
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.goodsEntries.entries.head.maybeGoodsVatRate mustBe Some(GoodsVatRate.Twenty)
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        val goodsEntry = GoodsEntry(CategoryQuantityOfGoods("test good", "123"))

        givenADeclarationJourneyIsPersisted(
          startedDeclarationJourney.copy(goodsEntries = GoodsEntries(goodsEntry)))
        form.bindFromRequest()(postRequest)

        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        ensureContent(result, goodsEntry) must include("Select one of the options below")
      }
    }
  }
}

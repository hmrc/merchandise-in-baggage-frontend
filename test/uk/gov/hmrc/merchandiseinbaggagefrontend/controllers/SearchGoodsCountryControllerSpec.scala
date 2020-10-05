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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.SearchGoodsCountryFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.{CategoryQuantityOfGoods, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CountriesService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.SearchGoodsCountryView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SearchGoodsCountryControllerSpec extends DeclarationJourneyControllerSpec {
  private val formProvider = new SearchGoodsCountryFormProvider()
  private val form = formProvider(CountriesService.countries)

  private lazy val controller =
    new SearchGoodsCountryController(
      controllerComponents, actionBuilder, formProvider, declarationJourneyRepository, injector.instanceOf[SearchGoodsCountryView])

  private def ensureContent(result: Future[Result], goodsEntry: GoodsEntry) = {
    val content = contentAsString(result)

    content must include(s"In what country did you buy the ${goodsEntry.categoryQuantityOfGoods.category}?")
    content must include("If you bought this item on a plane or boat, enter the country you were travelling from at the time of purchase")
    content must include("Continue")

    content
  }

  "onPageLoad" must {
    val url = routes.SearchGoodsCountryController.onPageLoad().url
    val getRequest = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "redirect to /search-goods" when {
      "a declaration has been started but a required answer is missing in the journey" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad()(getRequest)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SearchGoodsController.onPageLoad().toString
      }
    }

    "return OK and render the view" when {
      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = Seq(completedGoodsEntry)))

        val result = controller.onPageLoad()(getRequest)

        status(result) mustEqual OK
        ensureContent(result, completedGoodsEntry)
      }
    }
  }

  "onSubmit" must {
    val url = routes.SearchGoodsCountryController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /value-weight-of-goods" when {
      "a declaration is started and a valid selection submitted" in {
        val before =
          startedDeclarationJourney.copy(goodsEntries = Seq(GoodsEntry(CategoryQuantityOfGoods("test good", "123"))))

        givenADeclarationJourneyIsPersisted(before)

        val request = postRequest.withFormUrlEncodedBody(("value", "Austria"))

        form.bindFromRequest()(request)

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SkeletonJourneyController.purchaseDetails().toString

        before.goodsEntries.head mustBe GoodsEntry(CategoryQuantityOfGoods("test good", "123"))
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.goodsEntries.head.maybeCountryOfPurchase mustBe Some("Austria")
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        val goodsEntry = GoodsEntry(CategoryQuantityOfGoods("test good", "123"))

        givenADeclarationJourneyIsPersisted(
          startedDeclarationJourney.copy(goodsEntries = Seq(goodsEntry)))
        form.bindFromRequest()(postRequest)

        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        ensureContent(result, goodsEntry) must include("Select a country")
      }
    }
  }
}

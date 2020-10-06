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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.ReviewGoodsFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.{CategoryQuantityOfGoods, Currency, CurrencyAmount, Goods, GoodsEntry, PriceOfGoods}
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ReviewGoodsView

class ReviewGoodsControllerSpec extends DeclarationJourneyControllerSpec {
  private val formProvider = new ReviewGoodsFormProvider()
  private val form = formProvider()

  private lazy val view = injector.instanceOf[ReviewGoodsView]

  private lazy val controller =
    new ReviewGoodsController(
      controllerComponents, actionBuilder, formProvider, view)

  val testGood =
    GoodsEntry(
      CategoryQuantityOfGoods("test good", "123"),
      Some("test vat rate"),
      Some("Austria"),
      Some(PriceOfGoods(CurrencyAmount(10.00), Currency("test currency", "TST"))),
      Some("test invoice number"),
      Some(CurrencyAmount(0.00))
    )

  "onPageLoad" must {
    val url = routes.ReviewGoodsController.onPageLoad().url
    val request = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "redirect to /invalid-request" when {
      "a declaration has been started but a required answer is missing in the journey" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.InvalidRequestController.onPageLoad().toString
      }
    }

    "return OK and render the view" when {
      "a declaration has been started with and goods have been entered" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = Seq(testGood)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, Seq(Goods(testGood)))(request, messagesApi.preferred(request), appConfig).toString
      }
    }
  }

  "onSubmit" must {
    val url = routes.ReviewGoodsController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /tax-calculation" when {
      "a declaration is started and false is submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = Seq(testGood)))

        val request = postRequest.withFormUrlEncodedBody(("value", "false"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SkeletonJourneyController.taxCalculation().toString
      }
    }

    "Redirect to /search-goods" when {
      "a declaration is started and true is submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = Seq(testGood)))

        val request = postRequest.withFormUrlEncodedBody(("value", "true"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SearchGoodsController.onPageLoad().toString
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = Seq(testGood)))

        val submittedForm = form.bindFromRequest()(postRequest)
        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(submittedForm, Seq(Goods(testGood)))(postRequest, messagesApi.preferred(postRequest), appConfig).toString
      }
    }
  }
}

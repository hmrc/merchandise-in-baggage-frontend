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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.Currency
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ReviewGoodsView
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.ReviewGoodsForm.form

import scala.concurrent.ExecutionContext.Implicits.global

class ReviewGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val view = injector.instanceOf[ReviewGoodsView]
  private lazy val controller =
    new ReviewGoodsController(controllerComponents, actionBuilder, declarationJourneyRepository, view)

  private val goods =
    GoodsEntry(
      Some(CategoryQuantityOfGoods("test good", "123")),
      Some(GoodsVatRates.Twenty),
      Some("Austria"),
      Some(PurchaseDetails("10.00", Currency("test country", "test currency", "TST"))),
      Some("test invoice number"),
      Some(0.00)
    )

  private val goodsEntries = GoodsEntries(goods)
  private val declarationGoods = DeclarationGoods(goods.goodsIfComplete.get)

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
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = goodsEntries))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, declarationGoods)(request, messagesApi.preferred(request), appConfig).toString
      }
    }
  }

  "onSubmit" must {
    val url = routes.ReviewGoodsController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /tax-calculation" when {
      "a declaration is started and false is submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = goodsEntries))

        val request = postRequest.withFormUrlEncodedBody(("value", "false"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SkeletonJourneyController.taxCalculation().toString
      }
    }

    "Redirect to /search-goods" when {
      "a declaration is started and true is submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = goodsEntries))

        val request = postRequest.withFormUrlEncodedBody(("value", "true"))
        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SearchGoodsController.onPageLoad(2).toString
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = goodsEntries))

        val submittedForm = form.bindFromRequest()(postRequest)
        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(submittedForm, declarationGoods)(postRequest, messagesApi.preferred(postRequest), appConfig).toString
      }
    }
  }
}

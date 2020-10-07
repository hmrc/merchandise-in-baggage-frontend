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
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpecWithWireMock
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.PurchaseDetailsFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.Currency
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.{CategoryQuantityOfGoods, GoodsEntries, GoodsEntry, PurchaseDetails}
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.CurrencyConversionStub.givenCurrenciesAreFound
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.PurchaseDetailsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PurchaseDetailsControllerSpec extends DeclarationJourneyControllerSpec with BaseSpecWithWireMock {
  private val formProvider = new PurchaseDetailsFormProvider()
  private val form = formProvider()

  private lazy val controller =
    new PurchaseDetailsController(
      controllerComponents, injector.instanceOf[HttpClient], actionBuilder, formProvider, declarationJourneyRepository, injector.instanceOf[PurchaseDetailsView])

  private def ensureContent(result: Future[Result], goodsEntry: GoodsEntry) = {
    val content = contentAsString(result)

    content must include(s"How much did you pay for the ${goodsEntry.categoryQuantityOfGoods.category}?")
    content must include("Continue")

    content
  }

  "onPageLoad" must {
    val url = routes.SearchGoodsController.onPageLoad().url
    val getRequest = buildGet(url, sessionId)

    givenCurrenciesAreFound(currencyConversionMockServer)

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
    val url = routes.SearchGoodsController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    givenCurrenciesAreFound(currencyConversionMockServer)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /invoice-number" when {
      "a declaration is started and a valid selection submitted" in {
        val goodsEntry = GoodsEntry(CategoryQuantityOfGoods("test good", "123"))
        val before = startedDeclarationJourney.copy(goodsEntries = GoodsEntries(goodsEntry))
        givenADeclarationJourneyIsPersisted(before)

        val request = postRequest.withFormUrlEncodedBody(("price", "100.0"), ("currency", "ARS"))

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.InvoiceNumberController.onPageLoad().toString

        declarationJourneyRepository
          .findBySessionId(sessionId)
          .futureValue
          .get
          .goodsEntries
          .entries
          .head
          .maybePurchaseDetails mustBe Some(PurchaseDetails(100.0, Currency("Argentina", "Peso", "ARS")))
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = GoodsEntries(completedGoodsEntry)))
        form.bindFromRequest()(postRequest)

        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        ensureContent(result, completedGoodsEntry) must include("Enter an amount")
      }
    }
  }
}

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
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.InvoiceNumberView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InvoiceNumberControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val controller =
    new InvoiceNumberController(
      controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[InvoiceNumberView])

  private def ensureContent(result: Future[Result], goodsEntry: GoodsEntry) = {
    val content = contentAsString(result)

    content must include(s"What is the invoice number for the ${goodsEntry.categoryQuantityOfGoods.category}?")
    content must include("This is the number on the original invoice you received for the goods.")
    content must include("Continue")

    content
  }

  "onPageLoad" must {
    val url = routes.InvoiceNumberController.onPageLoad().url
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
    val url = routes.InvoiceNumberController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "Redirect to /review-goods" when {
      "a declaration is started and a valid selection submitted" in {
        val before =
          startedDeclarationJourney.copy(goodsEntries = GoodsEntries(GoodsEntry(CategoryQuantityOfGoods("test good", "123"))))

        givenADeclarationJourneyIsPersisted(before)

        val request = postRequest.withFormUrlEncodedBody(("value", "test invoice number"))

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.ReviewGoodsController.onPageLoad().toString

        before.goodsEntries.entries.head mustBe GoodsEntry(CategoryQuantityOfGoods("test good", "123"))
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.goodsEntries.entries.head.maybeInvoiceNumber mustBe Some("test invoice number")
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        val goodsEntry = GoodsEntry(CategoryQuantityOfGoods("test good", "123"))

        givenADeclarationJourneyIsPersisted(
          startedDeclarationJourney.copy(goodsEntries = GoodsEntries(goodsEntry)))

        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        ensureContent(result, goodsEntry) must include("Enter an invoice number")
      }
    }
  }
}

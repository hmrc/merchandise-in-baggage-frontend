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
  "onSubmit" must {
    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        val view = injector.instanceOf[ReviewGoodsView]
        val controller =
          new ReviewGoodsController(controllerComponents, actionBuilder, declarationJourneyRepository, view)

        val goods =
          GoodsEntry(
            Some(CategoryQuantityOfGoods("test good", "123")),
            Some(GoodsVatRates.Twenty),
            Some("Austria"),
            Some(PurchaseDetails("10.00", Currency("test country", "test currency", "TST"))),
            Some("test invoice number")
          )

        val goodsEntries = GoodsEntries(goods)
        val declarationGoods = DeclarationGoods(goods.goodsIfComplete.get)
        val postRequest = buildPost(routes.ReviewGoodsController.onSubmit().url, sessionId)
        val submittedForm = form.bindFromRequest()(postRequest)

        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(goodsEntries = goodsEntries))

        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(
            submittedForm,
            declarationGoods,
            routes.InvoiceNumberController.onPageLoad(1))(
            postRequest, messagesApi.preferred(postRequest), appConfig).toString
      }
    }
  }
}

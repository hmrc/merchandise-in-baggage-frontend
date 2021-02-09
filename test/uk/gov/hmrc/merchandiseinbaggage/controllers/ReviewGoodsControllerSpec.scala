/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.ReviewGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class ReviewGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = app.injector.instanceOf[ReviewGoodsView]

  def controller(declarationJourney: DeclarationJourney) =
    new ReviewGoodsController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  declarationTypes.foreach { importOrExport =>
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport, goodsEntries = completedGoodsEntries(importOrExport))

    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request = buildGet(routes.ReviewGoodsController.onPageLoad().url, aSessionId)
        val eventualResult = controller(journey).onPageLoad()(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi("reviewGoods.title"))
        result must include(messageApi("reviewGoods.heading"))
        result must include(messageApi("reviewGoods.list.item"))
        result must include(messageApi("reviewGoods.list.quantity"))

        if (importOrExport == Import) {
          result must include(messageApi("reviewGoods.list.vatRate"))
          result must include(messageApi("reviewGoods.list.producedInEu"))
        }

        if (importOrExport == Export) { result must include(messageApi("reviewGoods.list.destination")) }

        result must include(messageApi("reviewGoods.list.price"))
        result must include(messageApi("site.change"))
        result must include(messageApi("site.remove"))
        result must include(messageApi("reviewGoods.h3"))
      }
    }

    "onSubmit" should {
      s"redirect to next page after successful form submit with Yes for $importOrExport" in {

        val request = buildPost(routes.ReviewGoodsController.onSubmit().url, aSessionId)
          .withFormUrlEncodedBody("value" -> "Yes")

        val eventualResult = controller(journey).onSubmit(request)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.GoodsTypeQuantityController.onPageLoad(2).url)
      }

      s"redirect to next page after successful form submit with No for $importOrExport" in {

        val request = buildPost(routes.ReviewGoodsController.onSubmit().url, aSessionId)
          .withFormUrlEncodedBody("value" -> "No")

        val eventualResult = controller(journey).onSubmit(request)

        status(eventualResult) mustBe 303

        val redirectTo = importOrExport match {
          case Import => routes.PaymentCalculationController.onPageLoad().url
          case Export => routes.CustomsAgentController.onPageLoad().url
        }

        redirectLocation(eventualResult) mustBe Some(redirectTo)
      }
    }

    s"return 400 with any form errors for $importOrExport" in {

      val request = buildPost(routes.ReviewGoodsController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(journey).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messageApi("reviewGoods.title"))
      result must include(messageApi("reviewGoods.heading"))
    }
  }
}

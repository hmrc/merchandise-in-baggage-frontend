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

import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsDestinationView

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsDestinationControllerSpec extends DeclarationJourneyControllerSpec {

  val view = injector.instanceOf[GoodsDestinationView]

  def controller(declarationJourney: DeclarationJourney) =
    new GoodsDestinationController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  declarationTypes.foreach { importOrExport: DeclarationType =>
    val journey: DeclarationJourney = DeclarationJourney(aSessionId, importOrExport)
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request = buildGet(routes.GoodsDestinationController.onPageLoad.url, aSessionId)
        val eventualResult = controller(journey).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi(s"goodsDestination.$importOrExport.title"))
        result must include(messageApi(s"goodsDestination.$importOrExport.heading"))
        result must include(messageApi("goodsDestination.NorthernIreland"))
        result must include(messageApi("goodsDestination.GreatBritain"))
      }
    }

    "onSubmit" should {
      s"redirect to /excise-and-restricted-goods after successful form submit with GreatBritain for $importOrExport" in {
        val request = buildGet(routes.GoodsDestinationController.onSubmit().url, aSessionId)
          .withFormUrlEncodedBody("value" -> "GreatBritain")

        val eventualResult = controller(journey).onSubmit(request)
        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.ExciseAndRestrictedGoodsController.onPageLoad().url)
      }
    }

    s"return 400 with any form errors for $importOrExport" in {
      val request = buildGet(routes.GoodsDestinationController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(journey).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
    }
  }
}

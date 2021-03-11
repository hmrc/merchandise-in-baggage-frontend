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

import org.scalamock.scalatest.MockFactory
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsInVehicleView
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.Yes

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsInVehicleControllerSpec extends DeclarationJourneyControllerSpec with MockFactory {

  private val view = app.injector.instanceOf[GoodsInVehicleView]
  val mockNavigator = mock[Navigator]

  def controller(declarationJourney: DeclarationJourney) =
    new GoodsInVehicleController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), view, mockNavigator)

  declarationTypes.foreach { importOrExport =>
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport, goodsEntries = completedGoodsEntries(importOrExport))

    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request = buildGet(GoodsInVehicleController.onPageLoad().url, aSessionId)
        val eventualResult = controller(journey).onPageLoad()(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messages(s"goodsInVehicle.$importOrExport.title"))
        result must include(messages(s"goodsInVehicle.$importOrExport.heading"))
      }
    }

    "onSubmit" should {
      s"redirect by delegating to navigator for $importOrExport" in {

        val request = buildPost(GoodsInVehicleController.onSubmit().url, aSessionId)
          .withFormUrlEncodedBody("value" -> "Yes")

        (mockNavigator
          .nextPage(_: RequestWithAnswer[_]))
          .expects(RequestWithAnswer(GoodsInVehicleController.onPageLoad().url, Yes))
          .returning(VehicleSizeController.onPageLoad())

        controller(journey).onSubmit()(request).futureValue
      }
    }

    s"return 400 with any form errors for $importOrExport" in {

      val request = buildGet(GoodsInVehicleController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(journey).onSubmit()(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages(s"goodsInVehicle.$importOrExport.title"))
      result must include(messages(s"goodsInVehicle.$importOrExport.heading"))
    }
  }
}

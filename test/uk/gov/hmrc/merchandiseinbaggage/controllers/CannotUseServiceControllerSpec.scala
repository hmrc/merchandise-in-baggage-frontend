/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.CannotUseServiceView

class CannotUseServiceControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = app.injector.instanceOf[CannotUseServiceView]

  def controller(declarationJourney: DeclarationJourney): CannotUseServiceController =
    new CannotUseServiceController(controllerComponents, stubProvider(declarationJourney), view)

  declarationTypes.foreach { (importOrExport: DeclarationType) =>
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport, isAssistedDigital = false)
        .copy(maybeGoodsDestination = Some(GreatBritain))

    s"onPageLoad for $importOrExport" should {
      "return 200 with radio buttons" in {

        val request        = buildGet(routes.CannotUseServiceController.onPageLoad.url, aSessionId)
        val eventualResult = controller(journey).onPageLoad()(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe OK
        result must include(messages(s"cannotUseService.$importOrExport.title"))
        result must include(messages(s"cannotUseService.$importOrExport.heading"))
        result must include(messages(s"cannotUseService.$importOrExport.p1"))
      }

      "have different back url for different routes" in {
        val requestExciseAndRestrictedGoodsController =
          buildGet(routes.ExciseAndRestrictedGoodsController.onPageLoad.url, aSessionId)
        val eventualResult                            = controller(journey).onPageLoad()(requestExciseAndRestrictedGoodsController)
        val result                                    = contentAsString(eventualResult)
        result must include("excise-and-restricted-goods")

        val requestValueWeightOfGoodsController        =
          buildGet(routes.ValueWeightOfGoodsController.onPageLoad.url, aSessionId)
        val eventualResultValueWeightOfGoodsController =
          controller(journey).onPageLoad()(requestValueWeightOfGoodsController)
        val resultValueWeightOfGoodsController         = contentAsString(eventualResultValueWeightOfGoodsController)
        resultValueWeightOfGoodsController must include("value-weight-of-goods")

        val requestVehicleSizeController        = buildGet(routes.VehicleSizeController.onPageLoad.url, aSessionId)
        val eventualResultVehicleSizeController = controller(journey).onPageLoad()(requestVehicleSizeController)
        val resultVehicleSizeController         = contentAsString(eventualResultVehicleSizeController)
        resultVehicleSizeController must include("vehicle-size")
      }
    }
  }
}

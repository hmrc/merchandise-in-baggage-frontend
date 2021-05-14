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

package uk.gov.hmrc.merchandiseinbaggage.content

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.controllers.{DeclarationJourneyControllerSpec, GoodsOverThresholdController, routes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.{givenAPaymentCalculation, givenExchangeRateURL}
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsOverThresholdView
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsOverThresholdControllerSpec extends DeclarationJourneyControllerSpec with WireMockSupport {

  private val view = app.injector.instanceOf[GoodsOverThresholdView]
  private val calculatorService = app.injector.instanceOf[CalculationService]
  private val mibConnector = injector.instanceOf[MibConnector]

  def controller(declarationJourney: DeclarationJourney) =
    new GoodsOverThresholdController(controllerComponents, stubProvider(declarationJourney), calculatorService, mibConnector, view)

  declarationTypes.foreach { importOrExport: DeclarationType =>
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport)
        .copy(maybeGoodsDestination = Some(GreatBritain), goodsEntries = completedGoodsEntries(importOrExport))

    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenExchangeRateURL("http://something")

        givenAPaymentCalculation(aCalculationResult)

        val request = buildGet(routes.GoodsOverThresholdController.onPageLoad().url, aSessionId)
        val eventualResult = controller(journey).onPageLoad()(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi(s"goodsOverThreshold.GreatBritain.title"))
        result must include(messageApi(s"goodsOverThreshold.GreatBritain.heading"))
        result must include(messageApi(s"goodsOverThreshold.GreatBritain.$importOrExport.p1"))
        result must include(messageApi(s"goodsOverThreshold.p2"))
        result must include(messageApi(s"goodsOverThreshold.p2.$importOrExport.a.text"))
        result must include(messageApi(s"goodsOverThreshold.p2.$importOrExport.a.href"))
        if (importOrExport == Import) {
          result must include(messageApi(s"goodsOverThreshold.p8.1"))
          result must include("http://something")
          result must include(messageApi(s"goodsOverThreshold.p8.a.text"))
        }
      }
    }
  }
}

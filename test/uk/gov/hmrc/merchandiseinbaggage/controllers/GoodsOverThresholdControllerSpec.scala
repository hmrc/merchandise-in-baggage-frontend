/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.service.MibService
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsOverThresholdView

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsOverThresholdControllerSpec extends DeclarationJourneyControllerSpec {

  private val view              = app.injector.instanceOf[GoodsOverThresholdView]
  private val calculatorService = app.injector.instanceOf[MibService]

  def controller(declarationJourney: DeclarationJourney): GoodsOverThresholdController =
    new GoodsOverThresholdController(
      controllerComponents,
      stubProvider(declarationJourney),
      calculatorService,
      view
    )

  declarationTypes.foreach { importOrExport: DeclarationType =>
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport, isAssistedDigital = false)
        .copy(maybeGoodsDestination = Some(GreatBritain), goodsEntries = completedGoodsEntries(importOrExport))

    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenAPaymentCalculation(aCalculationResult)

        val request        = buildGet(routes.GoodsOverThresholdController.onPageLoad.url, aSessionId, journey)
        val eventualResult = controller(journey).onPageLoad()(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe OK
        result must include(messages("goodsOverThreshold.GreatBritain.title", thresholdValueInUI))
        result must include(messages("goodsOverThreshold.GreatBritain.heading", thresholdValueInUI))
        result must include(messages(s"goodsOverThreshold.GreatBritain.$importOrExport.p1", thresholdValueInUI))
        result must include(messages("goodsOverThreshold.p2"))
        result must include(messages(s"goodsOverThreshold.p2.$importOrExport.a.text"))
        if (importOrExport == Import) {
          result must include("https://www.gov.uk/government/collections/exchange-rates-for-customs-and-vat")
        }
      }
    }
  }
}

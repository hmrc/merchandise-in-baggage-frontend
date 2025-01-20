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
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.ValueWeightOfGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class ValueWeightOfGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  private val view                                                                     = injector.instanceOf[ValueWeightOfGoodsView]
  private val navigator                                                                = injector.instanceOf[Navigator]
  private val mibConnector                                                             = injector.instanceOf[MibConnector]
  def controller(declarationJourney: DeclarationJourney): ValueWeightOfGoodsController =
    new ValueWeightOfGoodsController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      navigator,
      mibConnector,
      view
    )

  declarationTypes.foreach { (importOrExport: DeclarationType) =>
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport, isAssistedDigital = false)
        .copy(maybeGoodsDestination = Some(GreatBritain))
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request        = buildGet(routes.ValueWeightOfGoodsController.onPageLoad.url, aSessionId, journey)
        val eventualResult = controller(journey).onPageLoad(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe OK
        result must include(messages("valueWeightOfGoods.GreatBritain.title", thresholdValueInUI))
        result must include(messages("valueWeightOfGoods.GreatBritain.heading", thresholdValueInUI))
      }
    }

    "onSubmit" should {
      s"redirect to /goods-type after successful form submit with Yes for $importOrExport" in {
        val request =
          buildPost(
            routes.ValueWeightOfGoodsController.onSubmit.url,
            aSessionId,
            journey,
            formData = Seq("value" -> "Yes")
          )

        val eventualResult = controller(journey).onSubmit(request)

        status(eventualResult) mustBe SEE_OTHER
        redirectLocation(eventualResult) mustBe Some(routes.GoodsTypeController.onPageLoad(1).url)
      }
    }

    s"return 400 with any form errors for $importOrExport" in {
      val request =
        buildPost(
          routes.ValueWeightOfGoodsController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("value" -> "in valid")
        )

      val eventualResult = controller(journey).onSubmit(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe BAD_REQUEST
      result must include(messages("error.summary.title"))
      result must include(messages("valueWeightOfGoods.GreatBritain.title", thresholdValueInUI))
      result must include(messages("valueWeightOfGoods.GreatBritain.heading", thresholdValueInUI))
    }
  }
}

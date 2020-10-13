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
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.TaxCalculationView

import scala.concurrent.ExecutionContext.Implicits.global

class TaxCalculationControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val view = injector.instanceOf[TaxCalculationView]
  private lazy val calculationService = injector.instanceOf[CalculationService]
  private lazy val controller =
    new TaxCalculationController(controllerComponents, actionBuilder, calculationService, view)

  "onPageLoad" must {
    val url = routes.TaxCalculationController.onPageLoad().url
    val request = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "redirect to /invalid-request" when {
      "a declaration has been started but a required answer is missing in the journey" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.InvalidRequestController.onPageLoad().toString
      }
    }

    "return OK and render the view" when {
      "a declaration has been started and goods have been entered" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(goodsEntries = GoodsEntries(completedGoodsEntry)))

        val result = controller.onPageLoad()(request)

        status(result) mustEqual OK
        contentAsString(result) must include(routes.CustomsAgentController.onPageLoad().url)
      }
    }
  }
}

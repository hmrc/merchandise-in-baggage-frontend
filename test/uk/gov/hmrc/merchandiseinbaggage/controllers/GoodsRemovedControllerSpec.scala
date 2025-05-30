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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsRemovedView

class GoodsRemovedControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = app.injector.instanceOf[GoodsRemovedView]

  def controller(declarationJourney: DeclarationJourney): GoodsRemovedController =
    new GoodsRemovedController(controllerComponents, stubProvider(declarationJourney), view)

  val journey: DeclarationJourney =
    DeclarationJourney(aSessionId, Import, isAssistedDigital = false).copy(maybeGoodsDestination = Some(GreatBritain))

  "onPageLoad" should {
    s"return 200 with expected content" in {

      val request        = buildGet(routes.GoodsRemovedController.onPageLoad.url, aSessionId, journey)
      val eventualResult = controller(journey).onPageLoad()(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe OK
      result must include(messages(s"goodsRemoved.title"))
      result must include(messages(s"goodsRemoved.heading"))
      result must include(messages(s"goodsRemoved.p1"))
      result must include(messages(s"goodsRemoved.link"))
    }
  }
}

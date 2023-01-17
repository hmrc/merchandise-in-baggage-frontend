/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries}
import uk.gov.hmrc.merchandiseinbaggage.views.html.SearchGoodsCountryView

import scala.concurrent.ExecutionContext.Implicits.global

class SearchGoodsCountryControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = injector.instanceOf[SearchGoodsCountryView]
  private val navigator = injector.instanceOf[Navigator]

  val controller: DeclarationJourney => SearchGoodsCountryController =
    declarationJourney =>
      new SearchGoodsCountryController(
        controllerComponents,
        stubProvider(declarationJourney),
        stubRepo(declarationJourney),
        navigator,
        view)

  val journey: DeclarationJourney =
    DeclarationJourney(SessionId("123"), Export, goodsEntries = GoodsEntries(Seq(completedExportGoods.copy(maybePurchaseDetails = None))))

  "onPageLoad" should {
    s"return 200 with correct content Export" in {
      val request = buildGet(routes.SearchGoodsCountryController.onPageLoad(1).url, aSessionId)
      val eventualResult = controller(journey).onPageLoad(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messages(s"searchGoodsCountry.title", "test good"))
      result must include(messages(s"searchGoodsCountry.heading", "test good"))

      result must not(include("United Kingdom"))
    }
  }

  "onSubmit" should {
    s"redirect to next page after successful form submit for Export" in {
      val request = buildPost(routes.SearchGoodsCountryController.onSubmit(1).url, aSessionId)
        .withFormUrlEncodedBody("country" -> "AF")

      val eventualResult = controller(journey).onSubmit(1)(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.ReviewGoodsController.onPageLoad.url)
    }

    s"return 400 with any form errors for Export" in {
      val request = buildPost(routes.SearchGoodsCountryController.onSubmit(1).url, aSessionId)
        .withFormUrlEncodedBody("country" -> "in valid")

      val eventualResult = controller(journey).onSubmit(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages(s"searchGoodsCountry.title", "test good"))
      result must include(messages(s"searchGoodsCountry.heading", "test good"))
    }
  }
}

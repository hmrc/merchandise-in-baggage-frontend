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
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries}
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsOriginView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class GoodsOriginControllerSpec extends DeclarationJourneyControllerSpec with MockFactory {

  private val view = app.injector.instanceOf[GoodsOriginView]
  val mockNavigator = mock[Navigator]

  def controller(declarationJourney: DeclarationJourney) =
    new GoodsOriginController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), view, mockNavigator)

  val journey = startedImportToGreatBritainJourney.copy(goodsEntries = GoodsEntries(completedImportGoods.copy(maybePurchaseDetails = None)))

  "onPageLoad" should {
    s"return 200 with radio buttons" in {
      val request = buildGet(GoodsOriginController.onPageLoad(1).url, aSessionId)
      val eventualResult = controller(journey).onPageLoad(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messageApi(s"goodsOrigin.title"))
      result must include(messageApi(s"goodsOrigin.heading"))
    }
  }

  "onSubmit" should {
    s"redirect to /purchase-details/1 after successful form submit with Yes by delegating to Navigator" in {
      val request = buildPost(GoodsOriginController.onSubmit(1).url, aSessionId)
        .withFormUrlEncodedBody("value" -> "Yes")

      (mockNavigator
        .nextPageWithCallBack(_: GoodsOriginRequest)(_: ExecutionContext))
        .expects(*, *)
        .returning(Future successful PurchaseDetailsController.onPageLoad(1))
        .once()

      controller(journey).onSubmit(1)(request).futureValue
    }
  }

  s"return 400 with any form errors" in {
    val request = buildPost(GoodsOriginController.onSubmit(1).url, aSessionId)
      .withFormUrlEncodedBody("value" -> "in valid")

    val eventualResult = controller(journey).onSubmit(1)(request)
    val result = contentAsString(eventualResult)

    status(eventualResult) mustBe 400
    result must include(messageApi("error.summary.title"))
    result must include(messageApi(s"goodsOrigin.title"))
    result must include(messageApi(s"goodsOrigin.heading"))
  }
}

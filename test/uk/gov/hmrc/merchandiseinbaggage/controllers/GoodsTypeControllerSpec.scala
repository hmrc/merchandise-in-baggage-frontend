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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsTypeView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class GoodsTypeControllerSpec extends DeclarationJourneyControllerSpec {

  private val view: GoodsTypeView = app.injector.instanceOf[GoodsTypeView]
  val mockNavigator: Navigator    = mock(classOf[Navigator])

  def controller(declarationJourney: DeclarationJourney): GoodsTypeController =
    new GoodsTypeController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      view,
      mockNavigator
    )

  declarationTypes.foreach { (importOrExport: DeclarationType) =>
    journeyTypes.foreach { journeyType =>
      val journey: DeclarationJourney =
        DeclarationJourney(aSessionId, importOrExport, isAssistedDigital = false)
          .copy(journeyType = journeyType)

      "onPageLoad" should {
        s"return 200 with radio buttons for $importOrExport for journeyType $journeyType" in {

          val request        = buildGet(GoodsTypeController.onPageLoad(1).url, aSessionId, journey)
          val eventualResult = controller(journey).onPageLoad(1)(request)
          val result         = contentAsString(eventualResult)

          status(eventualResult) mustBe OK
          result must include(messageApi(s"goodsType.$journeyType.title"))
          result must include(messageApi(s"goodsType.$journeyType.heading"))
          result must include(messageApi("goodsType.p"))
        }
      }

      "onSubmit" should {
        s"redirect to next page after successful form submit for $importOrExport for journeyType $journeyType" in {
          val request =
            buildPost(
              GoodsTypeController.onSubmit(1).url,
              aSessionId,
              journey,
              formData = Seq("category" -> "clothes")
            )

          val page: Call = if (importOrExport == Import) {
            GoodsVatRateController.onPageLoad(1)
          } else {
            SearchGoodsCountryController.onPageLoad(1)
          }

          when(mockNavigator.nextPage(any[GoodsTypeRequest])(any[ExecutionContext]))
            .thenReturn(Future.successful(page))

          val result: Future[Result] = controller(journey).onSubmit(1)(request)

          status(result) mustBe SEE_OTHER
          if (importOrExport == Import) {
            redirectLocation(result) mustBe Some("/declare-commercial-goods/goods-vat-rate/1")
          } else {
            redirectLocation(result) mustBe Some("/declare-commercial-goods/search-goods-country/1")
          }
        }

        s"return 400 with any form errors for $importOrExport for journeyType $journeyType" in {
          val request =
            buildPost(
              GoodsTypeController.onSubmit(1).url,
              aSessionId,
              journey,
              formData = Seq("xyz" -> "clothes", "abc" -> "1")
            )

          val eventualResult = controller(journey).onSubmit(1)(request)
          val result         = contentAsString(eventualResult)

          status(eventualResult) mustBe BAD_REQUEST
          result must include(messageApi("error.summary.title"))
          result must include(messageApi(s"goodsType.$journeyType.title"))
          result must include(messageApi(s"goodsType.$journeyType.heading"))
          result must include(messageApi("goodsType.p"))
        }
      }
    }
  }
}

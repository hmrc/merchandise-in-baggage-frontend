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
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsInVehicleView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class GoodsInVehicleControllerSpec extends DeclarationJourneyControllerSpec {

  private val view: GoodsInVehicleView = app.injector.instanceOf[GoodsInVehicleView]
  val mockNavigator: Navigator         = mock(classOf[Navigator])

  def controller(declarationJourney: DeclarationJourney): GoodsInVehicleController =
    new GoodsInVehicleController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      view,
      mockNavigator
    )

  declarationTypes.foreach { importOrExport =>
    val journey: DeclarationJourney =
      DeclarationJourney(
        aSessionId,
        importOrExport,
        isAssistedDigital = false,
        goodsEntries = completedGoodsEntries(importOrExport)
      )

    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request        = buildGet(GoodsInVehicleController.onPageLoad.url, aSessionId, journey)
        val eventualResult = controller(journey).onPageLoad()(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe OK
        result must include(messages(s"goodsInVehicle.$importOrExport.title"))
        result must include(messages(s"goodsInVehicle.$importOrExport.heading"))
      }
    }

    "onSubmit" should {
      s"redirect by delegating to navigator for $importOrExport" in {
        val request =
          buildPost(
            GoodsInVehicleController.onSubmit.url,
            aSessionId,
            journey,
            formData = Seq("value" -> "Yes")
          )

        when(mockNavigator.nextPage(any[GoodsInVehicleRequest])(any[ExecutionContext]))
          .thenReturn(Future.successful(VehicleSizeController.onPageLoad))

        val result: Future[Result] = controller(journey).onSubmit()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/declare-commercial-goods/vehicle-size")
      }
    }

    s"return 400 with any form errors for $importOrExport" in {

      val request =
        buildPost(
          GoodsInVehicleController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("value" -> "in valid")
        )

      val eventualResult = controller(journey).onSubmit()(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe BAD_REQUEST
      result must include(messages("error.summary.title"))
      result must include(messages(s"goodsInVehicle.$importOrExport.title"))
      result must include(messages(s"goodsInVehicle.$importOrExport.heading"))
    }
  }
}

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
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.VehicleSizeView

import scala.concurrent.ExecutionContext.Implicits.global

class VehicleSizeControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = injector.instanceOf[VehicleSizeView]
  private val navigator = injector.instanceOf[Navigator]

  def controller(declarationJourney: DeclarationJourney) =
    new VehicleSizeController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), navigator, view)

  declarationTypes.foreach { importOrExport =>
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport, goodsEntries = completedGoodsEntries(importOrExport))

    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request = buildGet(routes.VehicleSizeController.onPageLoad.url, aSessionId)
        val eventualResult = controller(journey).onPageLoad()(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messages(s"vehicleSize.$importOrExport.title"))
        result must include(messages(s"vehicleSize.$importOrExport.heading"))
        result must include(messages("vehicleSize.hint"))
      }
    }

    "onSubmit" should {
      s"redirect to /vehicle-registration-number after successful form submit with Yes for $importOrExport" in {
        val request = buildPost(routes.VehicleSizeController.onSubmit.url, aSessionId)
          .withFormUrlEncodedBody("value" -> "Yes")
        val eventualResult = controller(journey).onSubmit()(request)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.VehicleRegistrationNumberController.onPageLoad.url)
      }
    }

    s"return 400 with any form errors for $importOrExport" in {

      val request = buildGet(routes.VehicleSizeController.onSubmit.url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(journey).onSubmit()(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages(s"vehicleSize.$importOrExport.title"))
      result must include(messages(s"vehicleSize.$importOrExport.heading"))
      result must include(messages("vehicleSize.hint"))
    }
  }
}

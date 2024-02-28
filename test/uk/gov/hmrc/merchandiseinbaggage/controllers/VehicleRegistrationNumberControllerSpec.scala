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
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.VehicleRegistrationNumberView

import scala.concurrent.ExecutionContext.Implicits.global

class VehicleRegistrationNumberControllerSpec extends DeclarationJourneyControllerSpec {

  val view: VehicleRegistrationNumberView = injector.instanceOf[VehicleRegistrationNumberView]
  val navigator: Navigator                = injector.instanceOf[Navigator]

  def controller(declarationJourney: DeclarationJourney): VehicleRegistrationNumberController =
    new VehicleRegistrationNumberController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      navigator,
      view
    )

  declarationTypes.foreach { importOrExport =>
    val journey: DeclarationJourney = DeclarationJourney(aSessionId, importOrExport)

    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request        = buildGet(routes.VehicleRegistrationNumberController.onPageLoad.url, aSessionId)
        val eventualResult = controller(journey).onPageLoad()(request)

        status(eventualResult) mustBe OK
        contentAsString(eventualResult) must include(messages("vehicleRegistrationNumber.title"))
        contentAsString(eventualResult) must include(messages("vehicleRegistrationNumber.heading"))
        contentAsString(eventualResult) must include(messages("vehicleRegistrationNumber.hint"))
      }
    }

    "onSubmit" should {
      s"redirect to next page after successful form submit for $importOrExport" in {

        val request        = buildPost(routes.VehicleRegistrationNumberController.onSubmit.url, aSessionId)
          .withFormUrlEncodedBody("value" -> "KM04 123")

        val eventualResult = controller(journey).onSubmit()(request)

        status(eventualResult) mustBe SEE_OTHER
        redirectLocation(eventualResult) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
      }

      s"return 400 with required form error for $importOrExport" in {

        val request        = buildGet(routes.VehicleRegistrationNumberController.onSubmit.url, aSessionId)
          .withFormUrlEncodedBody("value123" -> "")

        val eventualResult = controller(journey).onSubmit()(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe BAD_REQUEST
        result must include(messageApi("error.summary.title"))
        result must include(messages("vehicleRegistrationNumber.error.required"))
      }
    }
  }
}

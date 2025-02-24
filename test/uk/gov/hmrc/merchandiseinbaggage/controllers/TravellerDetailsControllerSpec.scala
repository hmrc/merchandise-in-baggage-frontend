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
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.TravellerDetailsPage

import scala.concurrent.ExecutionContext.Implicits.global

class TravellerDetailsControllerSpec extends DeclarationJourneyControllerSpec {

  private val view      = injector.instanceOf[TravellerDetailsPage]
  private val navigator = injector.instanceOf[Navigator]

  val controller: DeclarationJourney => TravellerDetailsController =
    declarationJourney =>
      new TravellerDetailsController(
        controllerComponents,
        stubProvider(declarationJourney),
        stubRepo(declarationJourney),
        navigator,
        view
      )

  val journey: DeclarationJourney =
    DeclarationJourney(aSessionId, Import, isAssistedDigital = false).copy(maybeIsACustomsAgent = Some(YesNo.No))
  "onPageLoad" should {
    s"return 200 with correct content for" in {

      val request        = buildGet(routes.TravellerDetailsController.onPageLoad.url, aSessionId, journey)
      val eventualResult = controller(journey).onPageLoad(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe OK
      result must include(messages("travellerDetails.title"))
      result must include(messages("travellerDetails.heading"))
      result must include(messages("travellerDetails.hint"))
      result must include(messages("travellerDetails.firstName"))
      result must include(messages("travellerDetails.lastName"))
    }
  }

  "onSubmit" should {
    s"redirect to next page after successful form submit" in {
      val request =
        buildPost(
          routes.TravellerDetailsController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("firstName" -> "Foo", "lastName" -> "Bar")
        )

      val eventualResult = controller(journey).onSubmit(request)
      status(eventualResult) mustBe SEE_OTHER
      redirectLocation(eventualResult) mustBe Some(routes.EnterEmailController.onPageLoad.url)
    }

    s"return 400 with required form errors" in {
      val request =
        buildPost(
          routes.EoriNumberController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("firstName" -> "", "lastName" -> "")
        )

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe BAD_REQUEST
      result must include(messages("travellerDetails.title"))
      result must include(messages("travellerDetails.heading"))
      result must include(messages("travellerDetails.hint"))
      result must include(messages("travellerDetails.firstName"))
      result must include(messages("travellerDetails.lastName"))
      result must include(messages("travellerDetails.firstName.error.required"))
      result must include(messages("travellerDetails.lastName.error.required"))
    }
  }
}

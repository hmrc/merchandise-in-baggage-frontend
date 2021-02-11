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

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.EnterEmailView

import scala.concurrent.ExecutionContext.Implicits.global

class EnterEmailControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = app.injector.instanceOf[EnterEmailView]

  def controller(declarationJourney: DeclarationJourney) =
    new EnterEmailController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  val journey: DeclarationJourney = DeclarationJourney(aSessionId, Import)

  "onPageLoad" should {
    s"return 200 with correct content" in {

      val request = buildGet(routes.EnterEmailController.onPageLoad().url, aSessionId)
      val eventualResult = controller(journey).onPageLoad()(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messages("enterEmail.title"))
      result must include(messages("enterEmail.heading"))
      result must include(messages("enterEmail.email"))
      result must include(messages("enterEmail.hint"))
    }
  }

  "onSubmit" should {
    s"redirect to /journey-details after successful form submit" in {

      val request = buildPost(routes.EnterEmailController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("email" -> "test@email.com")
      val eventualResult = controller(journey).onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.JourneyDetailsController.onPageLoad().url)
    }
  }

  s"return 400 with any form errors" in {

    val request = buildPost(routes.EnterEmailController.onSubmit().url, aSessionId)
      .withFormUrlEncodedBody("email" -> "in valid")

    val eventualResult = controller(journey).onSubmit()(request)
    val result = contentAsString(eventualResult)

    status(eventualResult) mustBe 400
    result must include(messages("enterEmail.title"))
    result must include(messages("enterEmail.heading"))
    result must include(messages("enterEmail.email"))
    result must include(messages("enterEmail.hint"))
    result must include(messages("enterEmail.error.invalid"))
  }
}

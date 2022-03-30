/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.views.html.{EnterEmailView, EnterOptionalEmailView}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EnterEmailControllerSpec extends DeclarationJourneyControllerSpec with MockFactory {

  private val view = app.injector.instanceOf[EnterEmailView]
  private val viewOpt = app.injector.instanceOf[EnterOptionalEmailView]
  val mockNavigator = mock[Navigator]

  def controller(declarationJourney: DeclarationJourney) =
    new EnterEmailController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      view,
      viewOpt,
      mockNavigator)

  val journey: DeclarationJourney = DeclarationJourney(aSessionId, Import)

  //TODO move content test in UI
  "onPageLoad" should {
    s"return 200 with correct content" in {

      val request = buildGet(EnterEmailController.onPageLoad().url, aSessionId)
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
      val request = buildPost(EnterEmailController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("email" -> "test@email.com")

      (mockNavigator
        .nextPage(_: EnterEmailRequest)(_: ExecutionContext))
        .expects(*, *)
        .returning(Future.successful(JourneyDetailsController.onPageLoad()))
        .once()

      controller(journey).onSubmit()(request).futureValue
    }
  }

  s"return 400 with any form errors" in {
    val request = buildPost(EnterEmailController.onSubmit().url, aSessionId)
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

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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.views.html.{EnterEmailView, EnterOptionalEmailView}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EnterEmailControllerSpec extends DeclarationJourneyControllerSpec {

  private val view: EnterEmailView            = app.injector.instanceOf[EnterEmailView]
  private val viewOpt: EnterOptionalEmailView = app.injector.instanceOf[EnterOptionalEmailView]
  val mockNavigator: Navigator                = mock(classOf[Navigator])

  def controller(declarationJourney: DeclarationJourney): EnterEmailController =
    new EnterEmailController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      view,
      viewOpt,
      mockNavigator
    )

  val journey: DeclarationJourney = DeclarationJourney(aSessionId, Import, isAssistedDigital = false)

  // TODO move content test in UI
  "onPageLoad" should {
    "return 200 with correct content" in {

      val request        = buildGet(EnterEmailController.onPageLoad.url, aSessionId, journey)
      val eventualResult = controller(journey).onPageLoad()(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe OK
      result must include(messages("enterEmail.title"))
      result must include(messages("enterEmail.heading"))
      result must include(messages("enterEmail.email"))
      result must include(messages("enterEmail.hint"))
    }
  }

  "onSubmit" should {
    "redirect to /journey-details after successful form submit" in {
      val request =
        buildPost(
          EnterEmailController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("email" -> "test@email.com")
        )

      when(mockNavigator.nextPage(any[EnterEmailRequest])(any[ExecutionContext]))
        .thenReturn(Future.successful(JourneyDetailsController.onPageLoad))

      val result: Future[Result] = controller(journey).onSubmit()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/declare-commercial-goods/journey-details")
    }
  }

  "return 400 with any form errors" in {
    val request =
      buildPost(
        EnterEmailController.onSubmit.url,
        aSessionId,
        journey,
        formData = Seq("email" -> "in valid")
      )

    val eventualResult = controller(journey).onSubmit()(request)
    val result         = contentAsString(eventualResult)

    status(eventualResult) mustBe BAD_REQUEST
    result must include(messages("enterEmail.title"))
    result must include(messages("enterEmail.heading"))
    result must include(messages("enterEmail.email"))
    result must include(messages("enterEmail.hint"))
    result must include(messages("enterEmail.error.invalid"))
  }
}

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

import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.views.html.CustomsAgentView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CustomsAgentControllerSpec extends DeclarationJourneyControllerSpec {

  val view: CustomsAgentView                                   = app.injector.instanceOf[CustomsAgentView]
  val mockNavigator: Navigator                                 = mock[Navigator]
  val controller: DeclarationJourney => CustomsAgentController =
    declarationJourney =>
      new CustomsAgentController(
        controllerComponents,
        stubProvider(declarationJourney),
        stubRepo(declarationJourney),
        view,
        mockNavigator
      )

  private val journey: DeclarationJourney = DeclarationJourney(aSessionId, DeclarationType.Import)

  //TODO move content test in UI
  "onPageLoad" should {
    "return 200 with radio buttons" in {

      val request        = buildGet(CustomsAgentController.onPageLoad.url, aSessionId)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe OK
      result must include(messageApi("customsAgent.title"))
      result must include(messageApi("customsAgent.heading"))
      result must include(messageApi("customsAgent.hint"))
    }
  }

  "onSubmit" should {
    "delegate to Navigator" in {
      val request = buildGet(CustomsAgentController.onSubmit.url, aSessionId)
        .withFormUrlEncodedBody("value" -> "Yes")

      when(mockNavigator.nextPage(any[CustomsAgentRequest])(any[ExecutionContext]))
        .thenReturn(Future.successful(AgentDetailsController.onPageLoad))

      val result: Future[Result] = controller(journey).onSubmit(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/declare-commercial-goods/agent-details")
    }

    "return 400 with any form errors" in {
      val request        = buildGet(CustomsAgentController.onSubmit.url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(journey).onSubmit(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe BAD_REQUEST
      result must include(messageApi("error.summary.title"))
      result must include(messageApi("customsAgent.title"))
      result must include(messageApi("customsAgent.heading"))
    }
  }
}

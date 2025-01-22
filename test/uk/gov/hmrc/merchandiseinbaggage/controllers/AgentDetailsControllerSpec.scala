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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.views.html.AgentDetailsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AgentDetailsControllerSpec extends DeclarationJourneyControllerSpec {

  private val view: AgentDetailsView   = app.injector.instanceOf[AgentDetailsView]
  private val mockNavigator: Navigator = mock(classOf[Navigator])

  def controller(declarationJourney: DeclarationJourney): AgentDetailsController =
    new AgentDetailsController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      view,
      mockNavigator
    )

  val journey: DeclarationJourney =
    DeclarationJourney(aSessionId, Import, isAssistedDigital = false).copy(maybeGoodsDestination = Some(GreatBritain))

  "onPageLoad" should {
    "return 200 with correct content" in {

      val request        = buildGet(routes.AgentDetailsController.onPageLoad.url, aSessionId)
      val eventualResult = controller(journey).onPageLoad(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe OK
      result must include(messages("agentDetails.title"))
      result must include(messages("agentDetails.heading"))
    }
  }

  "onSubmit" should {
    "redirect to /enter-agent-address after successful form" in {
      val request =
        buildPost(
          routes.AgentDetailsController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("value" -> "business name")
        )

      when(mockNavigator.nextPage(any[AgentDetailsRequest])(any[ExecutionContext]))
        .thenReturn(Future.successful(EnterAgentAddressController.onPageLoad))

      val result: Future[Result] = controller(journey).onSubmit(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/declare-commercial-goods/enter-agent-address")
    }
  }

  "return 400 with any form errors" in {
    val request =
      buildPost(
        routes.AgentDetailsController.onSubmit.url,
        aSessionId,
        journey,
        formData = Seq("value1" -> "in valid")
      )

    val eventualResult: Future[Result] = controller(journey).onSubmit(request)
    val result                         = contentAsString(eventualResult)

    status(eventualResult) mustBe BAD_REQUEST
    result must include(messages("error.summary.title"))
    result must include(messages(s"agentDetails.title"))
    result must include(messages(s"agentDetails.heading"))
  }
}

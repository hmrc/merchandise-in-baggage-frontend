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

import org.scalamock.scalatest.MockFactory
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes.AgentDetailsController
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.AgentDetailsView

import scala.concurrent.ExecutionContext.Implicits.global

class AgentDetailsControllerSpec extends DeclarationJourneyControllerSpec with MockFactory {

  private val view = app.injector.instanceOf[AgentDetailsView]
  private val mockNavigator = mock[Navigator]

  def controller(declarationJourney: DeclarationJourney) =
    new AgentDetailsController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), view, mockNavigator)

  val journey: DeclarationJourney =
    DeclarationJourney(aSessionId, Import).copy(maybeGoodsDestination = Some(GreatBritain))

  "onPageLoad" should {
    s"return 200 with correct content" in {

      val request = buildGet(routes.AgentDetailsController.onPageLoad().url, aSessionId)
      val eventualResult = controller(journey).onPageLoad(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messageApi(s"agentDetails.title"))
      result must include(messageApi(s"agentDetails.heading"))
    }
  }

  "onSubmit" should {
    s"redirect to /enter-agent-address after successful form" in {
      val request = buildPost(routes.AgentDetailsController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("value" -> "business name")

      (mockNavigator
        .nextPage(_: RequestByPass))
        .expects(RequestByPass(AgentDetailsController.onPageLoad().url))
        .returning(routes.EnterAgentAddressController.onPageLoad())
        .once()

      controller(journey).onSubmit(request).futureValue
    }
  }

  s"return 400 with any form errors" in {
    val request = buildPost(routes.AgentDetailsController.onSubmit().url, aSessionId)
      .withFormUrlEncodedBody("value1" -> "in valid")

    val eventualResult = controller(journey).onSubmit(request)
    val result = contentAsString(eventualResult)

    status(eventualResult) mustBe 400
    result must include(messageApi("error.summary.title"))
    result must include(messageApi(s"agentDetails.title"))
    result must include(messageApi(s"agentDetails.heading"))
  }
}

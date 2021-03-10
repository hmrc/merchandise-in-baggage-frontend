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
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes.{AgentDetailsController, _}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.CustomsAgentView

import scala.concurrent.ExecutionContext.Implicits.global

class CustomsAgentControllerSpec extends DeclarationJourneyControllerSpec with MockFactory {

  val view = app.injector.instanceOf[CustomsAgentView]
  val mockNavigator = mock[Navigator]
  val controller: DeclarationJourney => CustomsAgentController =
    declarationJourney =>
      new CustomsAgentController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), view, mockNavigator)

  private val journey: DeclarationJourney = DeclarationJourney(aSessionId, DeclarationType.Import)

  "onPageLoad" should {
    s"return 200 with radio buttons" in {

      val request = buildGet(CustomsAgentController.onPageLoad().url, aSessionId)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messageApi("customsAgent.title"))
      result must include(messageApi("customsAgent.heading"))
      result must include(messageApi("customsAgent.hint"))
    }
  }

  "onSubmit" should {
    s"redirect to /agent-details on submit if answer is Yes" in {
      val request = buildGet(CustomsAgentController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("value" -> "Yes")

      (mockNavigator
        .nextPage(_: RequestWithYesNo))
        .expects(RequestWithYesNo(CustomsAgentController.onPageLoad().url, Yes))
        .returning(AgentDetailsController.onPageLoad())
        .once()

      val eventualResult = controller(journey).onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(AgentDetailsController.onPageLoad().url)
    }

    //TODO move this test to NavigatorSpec
    s"redirect to /eori-number on submit if answer is No" in {
      val request = buildGet(CustomsAgentController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("value" -> "No")

      (mockNavigator
        .nextPage(_: RequestWithYesNo))
        .expects(RequestWithYesNo(CustomsAgentController.onPageLoad().url, No))
        .returning(EoriNumberController.onPageLoad())
        .once()

      controller(journey).onSubmit(request).futureValue
    }

    s"return 400 with any form errors" in {
      val request = buildGet(CustomsAgentController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(journey).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messageApi("customsAgent.title"))
      result must include(messageApi("customsAgent.heading"))
    }
  }
}

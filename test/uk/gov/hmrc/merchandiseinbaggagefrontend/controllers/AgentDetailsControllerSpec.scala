/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.AgentDetailsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AgentDetailsControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val controller =
    new AgentDetailsController(
      controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[AgentDetailsView])

  private def ensureContent(result: Future[Result]) = {
    val content = contentAsString(result)

    content must include("Enter the business name of the customs agent")
    content must include("Continue")

    content
  }

  "onPageLoad" must {
    val url = routes.AgentDetailsController.onPageLoad().url
    val getRequest = buildGet(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller, url)

    "return OK and render the view" when {
      "a declaration has been started" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad()(getRequest)

        status(result) mustEqual OK
        ensureContent(result)
      }
    }

    "return OK and render the view" when {
      "a declaration has been started and a value saved" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney.copy(maybeCustomsAgentName = Some("test agent")))

        val result = controller.onPageLoad()(getRequest)

        status(result) mustEqual OK
        ensureContent(result) must include("test agent")
      }
    }
  }

  "onSubmit" must {
    val url = routes.AgentDetailsController.onSubmit().url
    val postRequest = buildPost(url, sessionId)

    behave like anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller, url)

    "redirect to /enter-agent-address" when {
      "a declaration is started and a valid selection submitted" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val request = postRequest.withFormUrlEncodedBody(("value", "test agent"))

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.EnterAgentAddressController.onPageLoad().toString

        startedDeclarationJourney.maybeCustomsAgentName mustBe None
        declarationJourneyRepository
          .findBySessionId(sessionId)
          .futureValue
          .get
          .maybeCustomsAgentName mustBe Some("test agent")
      }
    }

    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onSubmit()(postRequest)

        status(result) mustEqual BAD_REQUEST
        ensureContent(result) must include("Enter a value")
      }
    }
  }
}

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

import play.api.test.Helpers._

class CustomsAgentControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val controller = app.injector.instanceOf[CustomsAgentController]

  "render the page when a declaration journey has been started" in {
    val title = "Are you a customs agent?"
    val getRequest = buildGet(routes.CustomsAgentController.onPageLoad().url, sessionId)

    givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
    val eventualResponse = controller.onPageLoad()(getRequest)
    val content = contentAsString(eventualResponse)

    status(eventualResponse) mustBe OK
    content must include(title)
  }

  "redirect to agent Details on submit" in {
    val postRequest = buildPost(routes.CustomsAgentController.onSubmit().url, sessionId)

    givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
    val eventualResponse = controller.onSubmit()(postRequest)

    redirectLocation(eventualResponse) mustBe Some(routes.SkeletonJourneyController.agentDetails.url)
  }

//  "redirect to /invalid-request" when {
//    "a declaration journey has not been started" in {
//      ensureRedirectToInvalidRequestPage(controller.customsAgent(buildGet(url)))
//    }
//  }
}

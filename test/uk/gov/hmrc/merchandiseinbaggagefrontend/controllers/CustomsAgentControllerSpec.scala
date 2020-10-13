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

import akka.stream.Materializer
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo._

class CustomsAgentControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val controller = app.injector.instanceOf[CustomsAgentController]
  private implicit val materializer = app.injector.instanceOf[Materializer]

  "render the page when a declaration journey has been started" in {
    val title = "Are you a customs agent?"
    val getRequest = buildGet(routes.CustomsAgentController.onPageLoad().url, sessionId)

    givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
    val eventualResponse = controller.onPageLoad()(getRequest)
    val content = contentAsString(eventualResponse)

    status(eventualResponse) mustBe OK
    content must include(title)
  }

  "redirect to agent Details on submit if is an agent" in {
    val postRequest = buildPost(routes.CustomsAgentController.onSubmit().url, sessionId)
      .withFormUrlEncodedBody("value" -> YesNo.to(Yes).toString)

    givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
    val eventualResponse = controller.onSubmit()(postRequest)

    redirectLocation(eventualResponse) mustBe Some(routes.SkeletonJourneyController.agentDetails.url)
  }

  "redirect to EORI number on submit if is not an agent" in {
    val postRequest = buildPost(routes.CustomsAgentController.onSubmit().url, sessionId)
      .withFormUrlEncodedBody("value" -> YesNo.to(No).toString)

    givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
    val eventualResponse = controller.onSubmit()(postRequest)

    redirectLocation(eventualResponse) mustBe Some(routes.SkeletonJourneyController.enterEoriNumber().url)
  }
}

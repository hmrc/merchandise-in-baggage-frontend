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
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.SessionId
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.CheckYourAnswersPage

import scala.concurrent.ExecutionContext.Implicits.global

class CheckYourAnswersControllerSpec extends DeclarationJourneyControllerSpec {

  private val calculationService = injector.instanceOf[CalculationService]
  private val page = injector.instanceOf[CheckYourAnswersPage]

  val controller = new CheckYourAnswersController(controllerComponents, actionBuilder, calculationService, page)

  "on submit will calculate tax and send payment request to pay api" in {
    val sessionId = SessionId()
    givenADeclarationJourneyIsPersisted(completedDeclarationJourney.copy(sessionId = sessionId))
    val request = buildPost(routes.CheckYourAnswersController.onSubmit().url).withSession(SessionKeys.sessionId -> sessionId.value)

    status(controller.onSubmit()(request)) mustBe 200
  }
}

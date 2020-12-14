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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.core.SessionId
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.SessionExpiredView

import scala.concurrent.ExecutionContext.Implicits.global

class SessionExpiredControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  val view = app.injector.instanceOf[SessionExpiredView]
  val repo = app.injector.instanceOf[DeclarationJourneyRepository]
  val controller = new SessionExpiredController(controllerComponents, view)

  //TODO improve this test
  "return 200" in {
    val id = SessionId("unchanged")
    val journey = startedImportToGreatBritainJourney.copy(sessionId = id)
    givenADeclarationJourneyIsPersisted(journey)

    val result = controller.onPageLoad(buildGet(routes.SessionExpiredController.onPageLoad().url, id))

    status(result) mustBe 200
  }
}
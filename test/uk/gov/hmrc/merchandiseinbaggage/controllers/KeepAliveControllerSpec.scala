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

import java.time.LocalDateTime

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.core.SessionId
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.{ProgressDeletedView, ServiceTimeoutView}

import scala.concurrent.ExecutionContext.Implicits.global

class KeepAliveControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  val repo = app.injector.instanceOf[DeclarationJourneyRepository]
  val deletedView = app.injector.instanceOf[ProgressDeletedView]
  val serviceTimeoutView = app.injector.instanceOf[ServiceTimeoutView]
  val controller = new KeepAliveController(controllerComponents, actionBuilder, repo, deletedView, serviceTimeoutView)

  "return NoContent with no changes to declaration journey" in {
    val id = SessionId("unchanged")
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val journey = startedImportToGreatBritainJourney.copy(sessionId = id, createdAt = created)
    givenADeclarationJourneyIsPersisted(journey)

    val result = controller.onKeepAlive(buildGet(routes.KeepAliveController.onKeepAlive().url, id))

    status(result) mustBe 204
    repo.findBySessionId(id).futureValue mustBe Some(journey)
  }

  "return 200" in {
    val id = SessionId("unchanged")
    val journey = startedImportToGreatBritainJourney.copy(sessionId = id)
    givenADeclarationJourneyIsPersisted(journey)

    val result = controller.onProgressDelete(buildGet(routes.KeepAliveController.onProgressDelete().url, id))

    status(result) mustBe 200
  }
}

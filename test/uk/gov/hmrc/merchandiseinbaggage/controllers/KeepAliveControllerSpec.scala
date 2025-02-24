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

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.{ProgressDeletedView, ServiceTimeoutView}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class KeepAliveControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  val repo               = app.injector.instanceOf[DeclarationJourneyRepository]
  val deletedView        = app.injector.instanceOf[ProgressDeletedView]
  val serviceTimeoutView = app.injector.instanceOf[ServiceTimeoutView]
  val strideAction       = app.injector.instanceOf[StrideAuthAction]
  val controller         =
    new KeepAliveController(controllerComponents, actionBuilder, strideAction, repo, deletedView, serviceTimeoutView)

  "return NoContent with no changes to declaration journey" in {
    val id      = SessionId("unchanged")
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val journey = startedImportToGreatBritainJourney.copy(sessionId = id, createdAt = created)
    givenADeclarationJourneyIsPersisted(journey)

    val result = controller.onKeepAlive(buildGet(routes.KeepAliveController.onKeepAlive.url, id))

    status(result) mustBe 204
    repo.findBySessionId(id).futureValue mustBe Some(journey)
  }

  "return 200" in {
    val id      = SessionId("unchanged")
    val journey = startedImportToGreatBritainJourney.copy(sessionId = id)
    givenADeclarationJourneyIsPersisted(journey)

    val result = controller.onProgressDelete(buildGet(routes.KeepAliveController.onProgressDelete.url, id))

    status(result) mustBe OK
  }

  "onServiceTimeout" should {
    "return 200" in {
      val request = buildGet(routes.KeepAliveController.onServiceTimeout.url, aSessionId)

      val eventualResult = controller.onServiceTimeout()(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe OK

      result must include(messages(s"timeOut.title"))
      result must include(messages(s"timeOut.title"))
      result must include(messages(s"timeOut.guidance"))
      result must include(messages(s"timeOut.restart.p"))

      result must include(s"""href="https://www.gov.uk/" class="govuk-header__link""") // note: footer also has gov.uk
    }
  }
}

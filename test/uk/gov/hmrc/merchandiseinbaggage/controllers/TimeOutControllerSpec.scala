package uk.gov.hmrc.merchandiseinbaggage.controllers

import java.time.LocalDateTime

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.core.SessionId
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository

import scala.concurrent.ExecutionContext.Implicits.global

class TimeOutControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  val repo = app.injector.instanceOf[DeclarationJourneyRepository]
  val controller = new TimeOutController(controllerComponents, actionBuilder, repo)


  "return NoContent with no changes to declaration journey" in {
    val id = SessionId("unchanged")
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val journey = startedImportToGreatBritainJourney.copy(sessionId = id, createdAt = created)
    givenADeclarationJourneyIsPersisted(journey)

    val result = controller.onPageLoad(buildGet(routes.TimeOutController.onPageLoad().url, id))

    status(result) mustBe 204
    repo.findBySessionId(id).futureValue mustBe Some(journey)
  }
}

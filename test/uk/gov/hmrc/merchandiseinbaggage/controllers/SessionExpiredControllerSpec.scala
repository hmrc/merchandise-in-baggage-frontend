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

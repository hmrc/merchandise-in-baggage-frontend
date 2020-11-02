package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationType, SessionId}
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository

import scala.concurrent.ExecutionContext.Implicits.global

class DeclarationConfirmationControllerSpec extends DeclarationJourneyControllerSpec {

  val repo = app.injector.instanceOf[DeclarationJourneyRepository]
  val controller = new DeclarationConfirmationController(controllerComponents, actionBuilder, repo)

  "on page load return 200" in {
    val sessionId = SessionId()
    givenADeclarationJourneyIsPersisted(completedDeclarationJourney
      .copy(sessionId = sessionId)
      .copy(declarationType = DeclarationType.Export)
    )
    val request = buildGet(routes.CheckYourAnswersController.onPageLoad().url).withSession(SessionKeys.sessionId -> sessionId.value)

    val eventualResult = controller.onPageLoad()(request)

    status(eventualResult) mustBe 200
  }
}

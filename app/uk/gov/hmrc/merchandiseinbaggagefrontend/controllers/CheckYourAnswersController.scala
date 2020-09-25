/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.{Declaration, SessionId}
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.CheckYourAnswersPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(repo: DeclarationJourneyRepository, mcc: MessagesControllerComponents, page: CheckYourAnswersPage)
                                          (implicit val ec: ExecutionContext, appConfig: AppConfig)
  extends FrontendController(mcc) {


  val onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    def error = throw new RuntimeException("Unable to retrieve declaration journey")

    request.session.get(SessionKeys.sessionId).fold(error){ sessionId =>
      repo.findBySessionId(SessionId(sessionId)).map{
        case Some(declarationJourney) => Ok(page(Declaration(declarationJourney)))
        case _ => error
      }
    }
  }
}

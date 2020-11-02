package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository

import scala.concurrent.{ExecutionContext, Future}

class DeclarationConfirmationController @Inject()(
                                                   override val controllerComponents: MessagesControllerComponents,
                                                   actionProvider: DeclarationJourneyActionProvider,
                                                   repo: DeclarationJourneyRepository,
                                                 )(implicit ec: ExecutionContext, appConf: AppConfig) extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    Future.successful(Ok("onPageLoad"))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    Future.successful(Ok("onSubmit"))
  }
}

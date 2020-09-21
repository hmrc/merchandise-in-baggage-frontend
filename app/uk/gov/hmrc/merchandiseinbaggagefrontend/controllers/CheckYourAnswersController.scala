/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.CheckYourAnswersPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(mcc: MessagesControllerComponents, page: CheckYourAnswersPage)
                                          (implicit val ec: ExecutionContext, appConfig: AppConfig, errorHandler: ErrorHandler)
  extends FrontendController(mcc) {

  val checkYourAnswers: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(page()))
  }
}

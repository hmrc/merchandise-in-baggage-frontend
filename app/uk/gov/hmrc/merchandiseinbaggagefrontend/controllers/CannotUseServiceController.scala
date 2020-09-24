/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.CannotUseServiceView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

@Singleton
class CannotUseServiceController @Inject()(
                                          override val controllerComponents: MessagesControllerComponents,
                                          view: CannotUseServiceView
                                          )(implicit val appConfig: AppConfig) extends FrontendBaseController {


  def onPageLoad(): Action[AnyContent] = Action { implicit request =>
    Ok(view())
  }

}

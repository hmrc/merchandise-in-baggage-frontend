/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.GoodsDestinationFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestination
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.GoodsDestinationView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

@Singleton
class GoodsDestinationController @Inject()(
                                          override val controllerComponents: MessagesControllerComponents,
                                          formProvider: GoodsDestinationFormProvider,
                                          view: GoodsDestinationView
                                          )(implicit val appConfig: AppConfig) extends FrontendBaseController {

  val form: Form[GoodsDestination] = formProvider()

  def onPageLoad(): Action[AnyContent] = Action { implicit request =>
    Ok(view(form))
  }

  //TODO implement once session storage has been done under MIBM-77
  def onSubmit(): Action[AnyContent] = Action { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(view(formWithErrors)),
        value => Ok
      )
  }

}

/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.ExciseAndRestrictedGoodsFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ExciseAndRestrictedGoodsView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

@Singleton
class ExciseAndRestrictedGoodsController @Inject()(
                                          override val controllerComponents: MessagesControllerComponents,
                                          formProvider: ExciseAndRestrictedGoodsFormProvider,
                                          view: ExciseAndRestrictedGoodsView
                                          )(implicit val appConfig: AppConfig) extends FrontendBaseController {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(): Action[AnyContent] = Action { implicit request =>
    Ok(view(form))
  }

  //TODO implement once session storage has been done under MIBM-77
  def onSubmit(): Action[AnyContent] = Action { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(view(formWithErrors)),
        value =>
          value match {
            case true => Redirect(routes.CannotUseServiceController.onPageLoad())
            case false => Redirect(routes.GoodsDestinationController.onPageLoad())
          }
      )
  }

}

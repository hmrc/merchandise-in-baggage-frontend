/*
 * Copyright 2020 HM Revenue & Customs
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

  val onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(view(form))
  }

  //TODO implement once session storage has been done under MIBM-77
  val onSubmit: Action[AnyContent] = Action { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(view(formWithErrors)),
        value => Redirect(routes.SkeletonJourneyController.valueWeightOfGoods())
      )
  }

}

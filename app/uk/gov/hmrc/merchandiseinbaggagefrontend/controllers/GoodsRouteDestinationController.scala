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

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.GoodsRouteDestinationForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationType._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.No
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.GoodsRouteDestinationView

class GoodsRouteDestinationController @Inject()(
                                        override val controllerComponents: MessagesControllerComponents,
                                        actionProvider: DeclarationJourneyActionProvider,
                                        repo: DeclarationJourneyRepository,
                                        view: GoodsRouteDestinationView,
                                      )(implicit appConf: AppConfig) extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(view(form, request.declarationJourney.declarationType))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(view(formWithErrors, request.declarationJourney.declarationType)),
        value =>
          (value, request.declarationJourney.declarationType) match {
            case (No, _) => Redirect(routes.ExciseAndRestrictedGoodsController.onPageLoad())
            case (_, Import) => Redirect(routes.CannotUseServiceIrelandController.onPageLoad())
            case (_, Export) => Redirect(routes.NoDeclarationNeededController.onPageLoad())
          }
      )
  }
}

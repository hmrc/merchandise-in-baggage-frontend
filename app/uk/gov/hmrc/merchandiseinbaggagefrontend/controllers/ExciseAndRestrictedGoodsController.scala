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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.ExciseAndRestrictedGoodsForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.Yes
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ExciseAndRestrictedGoodsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExciseAndRestrictedGoodsController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                                   actionProvider: DeclarationJourneyActionProvider,
                                                   repo: DeclarationJourneyRepository,
                                                   view: ExciseAndRestrictedGoodsView)
                                                  (implicit ec: ExecutionContext, appConfig: AppConfig)
  extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(view(request.declarationJourney.maybeExciseOrRestrictedGoods.fold(form)(form.fill), request.declarationJourney.declarationType))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future successful BadRequest(view(formWithErrors, request.declarationJourney.declarationType)),
        value => {
          repo.upsert(request.declarationJourney.copy(maybeExciseOrRestrictedGoods = Some(value))).map { _ =>
            if (value == Yes) Redirect(routes.CannotUseServiceController.onPageLoad())
            else Redirect(routes.ValueWeightOfGoodsController.onPageLoad())
          }
        }
      )
  }
}

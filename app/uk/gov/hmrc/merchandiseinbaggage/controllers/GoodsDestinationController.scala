/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.{AmendDeclarationConfiguration, AppConfig}
import uk.gov.hmrc.merchandiseinbaggage.forms.GoodsDestinationForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.NorthernIreland
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.New
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsDestinationView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsDestinationController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: GoodsDestinationView
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController with AmendDeclarationConfiguration {

  private val backLink = if (amendFlagConf.canBeAmended) Some(routes.NewOrExistingController.onPageLoad()) else None

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(
      view(
        request.declarationJourney.maybeGoodsDestination
          .fold(form(request.declarationType))(form(request.declarationType).fill),
        request.declarationJourney.declarationType,
        backLink
      ))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form(request.declarationType)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.declarationJourney.declarationType, backLink))),
        value => {
          val redirectIfNotComplete =
            if (value == NorthernIreland)
              routes.CannotUseServiceIrelandController.onPageLoad()
            else
              routes.ExciseAndRestrictedGoodsController.onPageLoad()

          persistAndRedirect(request.declarationJourney.copy(maybeGoodsDestination = Some(value)), redirectIfNotComplete)
        }
      )
  }
}

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

import javax.inject.Inject
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.GoodsRouteDestinationForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType._
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.No
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsRouteDestinationView

import scala.concurrent.{ExecutionContext, Future}

class GoodsRouteDestinationController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: GoodsRouteDestinationView,
)(implicit appConf: AppConfig, executionContext: ExecutionContext)
    extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    backToCheckYourAnswersIfCompleteElse(routes.GoodsDestinationController.onPageLoad())

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(view(form, request.declarationJourney.declarationType, backButtonUrl))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    def updateAndRedirect(value: YesNo)(implicit request: DeclarationJourneyRequest[AnyContent]): Future[Result] =
      repo.upsert(request.declarationJourney.copy(maybeImportOrExportGoodsFromTheEUViaNorthernIreland = Some(value))).map { _ =>
        (value, request.declarationJourney.declarationType) match {
          case (No, _)     => Redirect(routes.ExciseAndRestrictedGoodsController.onPageLoad())
          case (_, Import) => Redirect(routes.CannotUseServiceIrelandController.onPageLoad())
          case (_, Export) => Redirect(routes.NoDeclarationNeededController.onPageLoad())
        }
      }

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.declarationJourney.declarationType, backButtonUrl))),
        value => updateAndRedirect(value)
      )
  }
}

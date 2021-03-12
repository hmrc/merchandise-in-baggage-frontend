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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.merchandiseinbaggage.config.{AmendDeclarationConfiguration, AppConfig}
import uk.gov.hmrc.merchandiseinbaggage.forms.NewOrExistingForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.NewOrExistingView
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyType

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NewOrExistingController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: NewOrExistingView,
  navigator: Navigator
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController with AmendDeclarationConfiguration {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    if (amendFlagConf.canBeAmended)
      Ok(
        view(
          form,
          request.declarationJourney.declarationType
        ))
    else Redirect(GoodsDestinationController.onPageLoad())
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future successful BadRequest(view(formWithErrors, request.declarationJourney.declarationType)),
        amendSession(_)
      )
  }

  private def amendSession(journeyType: JourneyType)(implicit request: DeclarationJourneyRequest[_]): Future[Result] =
    journeyType match {
      case New => Future successful redirect(journeyType)
      case Amend =>
        repo.upsert(request.declarationJourney.copy(journeyType = Amend)).map { _ =>
          redirect(journeyType)
        }
    }

  private def redirect(journeyType: JourneyType)(implicit req: DeclarationJourneyRequest[_]): Result =
    Redirect(navigator.nextPage(RequestWithAnswer(NewOrExistingController.onPageLoad().url, journeyType)))
      .addingToSession("journeyType" -> s"${journeyType.toString.toLowerCase}")
}

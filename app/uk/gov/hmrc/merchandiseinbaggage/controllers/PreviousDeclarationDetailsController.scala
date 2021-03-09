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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.service.DeclarationService
import uk.gov.hmrc.merchandiseinbaggage.views.html.PreviousDeclarationDetailsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PreviousDeclarationDetailsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  previousDeclarationDetailsService: DeclarationService,
  mibConnector: MibConnector,
  view: PreviousDeclarationDetailsView)(implicit ec: ExecutionContext, appConf: AppConfig)
    extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    previousDeclarationDetailsService.findDeclaration(request.declarationJourney.declarationId).map {
      _.fold(actionProvider.invalidRequest(s"declaration not found for id:${request.declarationJourney.declarationId.value}")) {
        case (goods, journeyDetails, declarationType, withinDate) =>
          Ok(view(goods, journeyDetails, declarationType, withinDate))
      }
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    mibConnector.findDeclaration(request.declarationJourney.declarationId).flatMap { maybeOriginalDeclaration =>
      maybeOriginalDeclaration
        .fold(actionProvider.invalidRequestF(s"declaration not found for id:${request.declarationJourney.declarationId.value}")) {
          originalDeclaration =>
            repo.upsert(request.declarationJourney.copy(maybeGoodsDestination = Some(originalDeclaration.goodsDestination))).map { _ =>
              Redirect(routes.ExciseAndRestrictedGoodsController.onPageLoad())
            }
        }
    }
  }
}

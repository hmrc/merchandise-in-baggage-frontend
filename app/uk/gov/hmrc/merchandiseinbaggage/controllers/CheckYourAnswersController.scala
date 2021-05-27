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
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.incompleteMessage
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.core.GoodsEntries
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository

import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  newHandler: CheckYourAnswersNewHandler,
  amendHandler: CheckYourAnswersAmendHandler,
  override val repo: DeclarationJourneyRepository)(implicit ec: ExecutionContext)
    extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    import request.declarationJourney._
    journeyType match {
      case New =>
        (for {
          declaration <- declarationIfRequiredAndComplete
          agent       <- maybeIsACustomsAgent
        } yield newHandler.onPageLoad(declaration, agent))
          .getOrElse(actionProvider.invalidRequestF(incompleteMessage))
      case Amend =>
        (for {
          amendment <- amendmentIfRequiredAndComplete
          agent     <- maybeIsACustomsAgent
        } yield
          amendHandler
            .onPageLoad(request.declarationJourney, amendment, agent)).getOrElse(actionProvider.invalidRequestF(incompleteMessage))
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.journeyType match {
      case New =>
        request.declarationJourney.declarationIfRequiredAndComplete
          .fold(actionProvider.invalidRequestF(incompleteMessage)) { declaration =>
            newHandler.onSubmit(declaration.copy(lang = messages.lang.code))
          }
      case Amend =>
        request.declarationJourney.amendmentIfRequiredAndComplete
          .fold(actionProvider.invalidRequestF(incompleteMessage)) { amendment =>
            amendHandler.onSubmit(request.declarationJourney.declarationId, amendment.copy(lang = messages.lang.code))
          }
    }
  }

  val addMoreGoods: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    val updatedGoodsEntries: GoodsEntries = request.declarationJourney.goodsEntries.addEmptyIfNecessary()

    repo.upsert(request.declarationJourney.copy(goodsEntries = updatedGoodsEntries)).map { _ =>
      Redirect(routes.GoodsTypeController.onPageLoad(updatedGoodsEntries.entries.size))
    }
  }
}

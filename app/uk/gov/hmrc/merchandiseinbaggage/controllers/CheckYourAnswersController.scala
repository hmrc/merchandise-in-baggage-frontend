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
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.incompleteMessage
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.core.GoodsEntries
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  newHandler: CheckYourAnswersNewHandler,
  amendHandler: CheckYourAnswersAmendHandler,
  override val repo: DeclarationJourneyRepository)(implicit ec: ExecutionContext)
    extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.journeyType match {
      case New =>
        request.declarationJourney.declarationIfRequiredAndComplete
          .fold(actionProvider.invalidRequestF(incompleteMessage)) { declaration =>
            newHandler.onPageLoad(declaration)
          }
      case Amend =>
        request.declarationJourney.amendmentIfRequiredAndComplete
          .fold(actionProvider.invalidRequestF(incompleteMessage)) { amendment =>
            amendHandler.onPageLoad(request.declarationType, amendment, request.declarationJourney.declarationId)
          }
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.journeyType match {
      case New =>
        request.declarationJourney.declarationIfRequiredAndComplete
          .fold(actionProvider.invalidRequestF(incompleteMessage)) { declaration =>
            newHandler.onSubmit(declaration)
          }
      case Amend =>
        request.declarationJourney.amendmentIfRequiredAndComplete
          .fold(actionProvider.invalidRequestF(incompleteMessage)) { amendment =>
            //TODO: Implement
            Future.successful(Redirect(routes.CheckYourAnswersController.onPageLoad().url))
          }
    }
  }

  val addMoreGoods: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    val updatedGoodsEntries: GoodsEntries = request.declarationJourney.goodsEntries.addEmptyIfNecessary()

    repo.upsert(request.declarationJourney.copy(goodsEntries = updatedGoodsEntries)).map { _ =>
      Redirect(routes.GoodsTypeQuantityController.onPageLoad(updatedGoodsEntries.entries.size))
    }
  }
}

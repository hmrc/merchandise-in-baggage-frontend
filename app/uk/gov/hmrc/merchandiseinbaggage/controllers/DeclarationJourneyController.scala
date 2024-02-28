/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.utils.DeclarationJourneyLogger
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

trait DeclarationJourneyController extends FrontendBaseController {
  implicit def messages(implicit request: Request[_]): Messages = controllerComponents.messagesApi.preferred(request)

  val onPageLoad: Action[AnyContent]

  def backToCheckYourAnswersIfCompleteElse(
    backIfIncomplete: Call
  )(implicit request: DeclarationJourneyRequest[_]): Call =
    if (
      request.declarationJourney.declarationRequiredAndComplete || request.declarationJourney.amendmentRequiredAndComplete
    ) {
      routes.CheckYourAnswersController.onPageLoad
    } else {
      backIfIncomplete
    }
}

object DeclarationJourneyController {
  val incompleteMessage                 = "declaration journey is not required and complete"
  val goodsDestinationUnansweredMessage = "goods destination is unanswered"
  val goodsDeclarationIncompleteMessage = "goods declaration is incomplete"
  val declarationNotFoundMessage        = "original declaration was not found"
}

trait DeclarationJourneyUpdateController extends DeclarationJourneyController {
  val onSubmit: Action[AnyContent]

  val repo: DeclarationJourneyRepository

  def persistAndRedirect(updatedDeclarationJourney: DeclarationJourney, redirectIfNotComplete: Call)(implicit
    ec: ExecutionContext
  ): Future[Result] =
    repo.upsert(updatedDeclarationJourney).map { _ =>
      if (updatedDeclarationJourney.declarationRequiredAndComplete) {
        Redirect(routes.CheckYourAnswersController.onPageLoad)
      } else {
        Redirect(redirectIfNotComplete)
      }
    }
}

trait IndexedDeclarationJourneyController extends FrontendBaseController {
  implicit def messages(implicit request: Request[_]): Messages = controllerComponents.messagesApi.preferred(request)

  def onPageLoad(idx: Int): Action[AnyContent]

  def withGoodsCategory(
    goodsEntry: GoodsEntry
  )(f: String => Future[Result])(implicit request: DeclarationGoodsRequest[AnyContent]): Future[Result] =
    goodsEntry.maybeCategory match {
      case Some(c) => f(c)
      case None    =>
        DeclarationJourneyLogger.warn(
          s"Goods category not found so redirecting to ${routes.CannotAccessPageController.onPageLoad}"
        )
        Future successful Redirect(routes.CannotAccessPageController.onPageLoad)
    }

  def checkYourAnswersOrReviewGoodsElse(default: Call, index: Int)(implicit request: DeclarationGoodsRequest[_]): Call =
    (
      request.declarationJourney.declarationRequiredAndComplete,
      request.declarationJourney.goodsEntries.entries(index - 1).isComplete
    ) match {
      case (true, true)   =>
        routes.CheckYourAnswersController.onPageLoad // user clicked change link from /check-your-answers
      case (true, false)  => default // user clicked add more goods from /check-your-answers
      case (false, true)  => routes.ReviewGoodsController.onPageLoad // user clicked change link from /review-goods
      case (false, false) => default // normal journey flow / user is adding more goods from /review-goods
    }
}

trait IndexedDeclarationJourneyUpdateController extends IndexedDeclarationJourneyController

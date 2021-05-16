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

import com.google.inject.Inject
import controllers.Assets.Redirect
import play.api.mvc._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.utils.DeclarationJourneyLogger

import scala.concurrent.{ExecutionContext, Future}

class DeclarationJourneyActionProvider @Inject()(defaultActionBuilder: DefaultActionBuilder, repo: DeclarationJourneyRepository)(
  implicit ec: ExecutionContext) {
  val journeyAction: ActionBuilder[DeclarationJourneyRequest, AnyContent] =
    defaultActionBuilder andThen journeyActionRefiner

  def goodsAction(idx: Int): ActionBuilder[DeclarationGoodsRequest, AnyContent] =
    defaultActionBuilder andThen journeyActionRefiner andThen goodsActionRefiner(idx)

  def invalidRequest(warnMessage: String)(implicit request: RequestHeader): Result = {
    DeclarationJourneyLogger.warn(s"$warnMessage so redirecting to ${routes.CannotAccessPageController.onPageLoad()}")(request)
    Redirect(routes.CannotAccessPageController.onPageLoad())
  }

  def invalidRequestF(warningMessage: String)(implicit request: RequestHeader): Future[Result] =
    Future.successful(invalidRequest(warningMessage))

  private def journeyActionRefiner: ActionRefiner[Request, DeclarationJourneyRequest] =
    new ActionRefiner[Request, DeclarationJourneyRequest] {

      override protected def refine[A](request: Request[A]): Future[Either[Result, DeclarationJourneyRequest[A]]] =
        request.session.get(SessionKeys.sessionId) match {
          case None => Future successful Left(invalidRequest("Session Id not found")(request))
          case Some(sessionId) =>
            repo.findBySessionId(SessionId(sessionId)).map {
              case Some(declarationJourney) =>
                Right(new DeclarationJourneyRequest(declarationJourney, request))
              case _ => Left(invalidRequest(s"Persisted declaration journey not found for session: $sessionId")(request))
            }
        }

      override protected def executionContext: ExecutionContext = ec
    }

  private def goodsActionRefiner(idx: Int): ActionRefiner[DeclarationJourneyRequest, DeclarationGoodsRequest] =
    new ActionRefiner[DeclarationJourneyRequest, DeclarationGoodsRequest] {

      override protected def refine[A](request: DeclarationJourneyRequest[A]): Future[Either[Result, DeclarationGoodsRequest[A]]] =
        Future successful (request.declarationJourney.goodsEntries.entries.lift(idx - 1) match {
          case None =>
            Left(invalidRequest(s"Goods entry not found for index $idx")(request))
          case Some(goodsEntry) =>
            Right(new DeclarationGoodsRequest(request, goodsEntry))
        })

      override protected def executionContext: ExecutionContext = ec
    }
}

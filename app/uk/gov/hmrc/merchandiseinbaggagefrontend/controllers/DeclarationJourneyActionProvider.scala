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

import com.google.inject.Inject
import play.api.mvc._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.SessionId
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository

import scala.concurrent.{ExecutionContext, Future}

class DeclarationJourneyActionProvider @Inject()(defaultActionBuilder: DefaultActionBuilder, repo: DeclarationJourneyRepository)
                                                (implicit ec: ExecutionContext) {
  val journeyAction: ActionBuilder[DeclarationJourneyRequest, AnyContent] =
    defaultActionBuilder andThen journeyActionRefiner

  def journeyActionRefiner: ActionRefiner[Request, DeclarationJourneyRequest] =
    new ActionRefiner[Request, DeclarationJourneyRequest] {

      override protected def refine[A](request: Request[A]): Future[Either[Result, DeclarationJourneyRequest[A]]] = {
        def error = throw new RuntimeException("Unable to retrieve declaration journey")

        request.session.get(SessionKeys.sessionId).fold(error){ sessionId =>
          repo.findBySessionId(SessionId(sessionId)).map{
            case Some(declarationJourney) => Right(new DeclarationJourneyRequest(declarationJourney, request))
            case _ => Left(error)
          }
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }
}

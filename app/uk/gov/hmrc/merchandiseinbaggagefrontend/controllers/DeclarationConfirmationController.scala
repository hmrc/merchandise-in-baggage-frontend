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

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.MibReference
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.MibReferenceGenerator
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.DeclarationConfirmationView

import scala.concurrent.{ExecutionContext, Future}

class DeclarationConfirmationController @Inject()(
                                                   override val controllerComponents: MessagesControllerComponents,
                                                   actionProvider: DeclarationJourneyActionProvider,
                                                   view: DeclarationConfirmationView,
                                                   repo: DeclarationJourneyRepository
                                                 )(implicit ec: ExecutionContext, appConf: AppConfig)
  extends DeclarationJourneyUpdateController with MibReferenceGenerator {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.declarationIfRequiredAndComplete.fold(actionProvider.invalidRequestF) { declaration =>
      resetJourney.map(ref => Ok(view(declaration.copy(mibReference = Some(ref)))))
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    Future.successful(Ok("onSubmit"))
  }

  private def resetJourney(implicit request: DeclarationJourneyRequest[AnyContent]): Future[MibReference] = {
    import request.declarationJourney._
    for {
      _         <- repo.upsert(DeclarationJourney(sessionId, declarationType))
      reference <- Future.fromTry(mibReference)
    } yield reference
  }
}

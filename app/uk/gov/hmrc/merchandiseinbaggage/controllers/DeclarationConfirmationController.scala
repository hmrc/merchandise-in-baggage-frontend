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

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, Declaration, JourneyType, NotRequired, Paid}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched.*
import uk.gov.hmrc.merchandiseinbaggage.views.html.DeclarationConfirmationView

import scala.concurrent.{ExecutionContext, Future}

class DeclarationConfirmationController @Inject() (
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  view: DeclarationConfirmationView,
  connector: MibConnector,
  val repo: DeclarationJourneyRepository
)(implicit ec: ExecutionContext, appConf: AppConfig)
    extends DeclarationJourneyController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    val declarationId = request.declarationJourney.declarationId
    val journeyType   = request.declarationJourney.journeyType
    connector.findDeclaration(declarationId).flatMap {
      case Some(declaration) if canShowConfirmation(declaration, journeyType, request.isAssistedDigital) =>
        cleanAnswersAndConfirm(journeyType, declaration)
      case Some(_)                                                                                       =>
        clearAnswers()
          .map(_ => actionProvider.invalidRequest("declaration is found in the db, but can't show confirmation"))
      case _                                                                                             => actionProvider.invalidRequestF(s"declaration not found for id:${declarationId.value}")
    }
  }

  private def cleanAnswersAndConfirm(journeyType: JourneyType, declaration: Declaration)(implicit
    request: DeclarationJourneyRequest[AnyContent]
  ): Future[Result] =
    if (request.isAssistedDigital) {
      for {
        _   <- clearAnswers()
        res <-
          connector.calculatePayments(declaration.latestGoods.map(_.calculationRequest(declaration.goodsDestination)))
      } yield Ok(view(declaration, journeyType, res.results.totalTaxDue))
    } else {
      clearAnswers().map(_ => Ok(view(declaration, journeyType, AmountInPence(0))))
    }

  val makeAnotherDeclaration: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    import request.declarationJourney._
    repo.upsert(DeclarationJourney(sessionId, declarationType, isAssistedDigital = request.isAssistedDigital)) map {
      _ =>
        if (request.isAssistedDigital) {
          Redirect(routes.ImportExportChoiceController.onPageLoad)
        } else {
          Redirect(routes.GoodsDestinationController.onPageLoad)
        }
    }
  }

  val addGoodsToAnExistingDeclaration: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    import request.declarationJourney._
    repo.upsert(DeclarationJourney(sessionId, declarationType, isAssistedDigital = request.isAssistedDigital)) map {
      _ =>
        Redirect(routes.RetrieveDeclarationController.onPageLoad)
    }
  }

  private def clearAnswers()(implicit request: DeclarationJourneyRequest[AnyContent]): Future[DeclarationJourney] = {
    import request.declarationJourney._
    repo.upsert(
      DeclarationJourney(sessionId, declarationType, isAssistedDigital = request.isAssistedDigital)
        .copy(declarationId = declarationId)
    )
  }

  private def canShowConfirmation(
    declaration: Declaration,
    journeyType: JourneyType,
    isAssistedDigital: Boolean
  ): Boolean =
    (declaration.declarationType, journeyType, isAssistedDigital) match {
      case (Export, _, _)        => true
      case (Import, New, true)   =>
        declaration.paymentStatus.contains(Paid) || declaration.paymentStatus.contains(NotRequired)
      case (Import, New, _)      => declaration.paymentStatus.contains(NotRequired)
      case (Import, Amend, true) =>
        val latestAmendmentStatus = declaration.amendments.lastOption.flatMap(_.paymentStatus)
        latestAmendmentStatus.contains(Paid) || latestAmendmentStatus.contains(NotRequired)
      case (Import, Amend, _)    => declaration.amendments.lastOption.flatMap(_.paymentStatus).contains(NotRequired)
    }
}

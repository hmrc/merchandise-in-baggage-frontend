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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import reactivemongo.api.commands.UpdateWriteResult
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.{MibConnector, PaymentConnector}
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.incompleteMessage
import uk.gov.hmrc.merchandiseinbaggage.forms.CheckYourAnswersForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, PayApiRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersExportView, CheckYourAnswersImportView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                           actionProvider: DeclarationJourneyActionProvider,
                                           calculationService: CalculationService,
                                           connector: PaymentConnector,
                                           mibConnector: MibConnector,
                                           override val repo: DeclarationJourneyRepository,
                                           importView: CheckYourAnswersImportView,
                                           exportView: CheckYourAnswersExportView)
                                          (implicit ec: ExecutionContext, appConfig: AppConfig)
  extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.declarationIfRequiredAndComplete
      .fold(actionProvider.invalidRequestF(incompleteMessage)) { declaration =>
        request.declarationJourney.declarationType match {
          case Import =>
            calculationService.paymentCalculation(declaration.declarationGoods).map { paymentCalculations =>
              if (paymentCalculations.totalGbpValue.value > declaration.goodsDestination.threshold.value) {
                Redirect(routes.GoodsOverThresholdController.onPageLoad())
              } else Ok(importView(form, declaration, paymentCalculations.totalTaxDue))
            }
          case Export =>
            if (declaration.declarationGoods.goods.map(_.purchaseDetails.numericAmount).sum > declaration.goodsDestination.threshold.inPounds)
              Future successful Redirect(routes.GoodsOverThresholdController.onPageLoad())
            else Future successful Ok(exportView(form, declaration))
        }
      }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.declarationIfRequiredAndComplete
      .fold(actionProvider.invalidRequestF(incompleteMessage))(declaration => declarationConfirmation(declaration))
  }

  val addMoreGoods: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    val updatedGoodsEntries: GoodsEntries = request.declarationJourney.goodsEntries.addEmptyIfNecessary()

    repo.upsert(request.declarationJourney.copy(goodsEntries = updatedGoodsEntries)).map { _ =>
      Redirect(routes.GoodsTypeQuantityController.onPageLoad(updatedGoodsEntries.entries.size))
    }
  }

  private def declarationConfirmation(declaration: Declaration)
                                     (implicit request: DeclarationJourneyRequest[AnyContent]): Future[Result] = {
    declaration.declarationType match {
      case Export =>
        continueExportDeclaration(declaration)
      case Import =>
        continueImportDeclaration(declaration)
    }
  }

  private def continueExportDeclaration(declaration: Declaration)(implicit request: DeclarationJourneyRequest[AnyContent]) =
    for {
      declarationId <- mibConnector.persistDeclaration(declaration)
      _ <- resetJourney(declarationId)
      _ <- mibConnector.sendEmails(declarationId)
    } yield Redirect(routes.DeclarationConfirmationController.onPageLoad())

  private def continueImportDeclaration(declaration: Declaration)
                                       (implicit request: DeclarationJourneyRequest[AnyContent]): Future[Result] =
    for {
      taxDue <- calculationService.paymentCalculation(declaration.declarationGoods)
      declarationId <- mibConnector.persistDeclaration(declaration.copy(maybeTotalCalculationResult = Some(taxDue.totalCalculationResult)))
      payApiResponse <- connector.sendPaymentRequest(PayApiRequest(declaration.mibReference, taxDue.totalTaxDue, taxDue.totalDutyDue, taxDue.totalVatDue))
      _ <- resetJourney(declarationId)
    } yield Redirect(payApiResponse.nextUrl.value)

  private def resetJourney(id: DeclarationId)(implicit request: DeclarationJourneyRequest[AnyContent]): Future[UpdateWriteResult] = {
    import request.declarationJourney._
    repo.upsert(DeclarationJourney(sessionId, declarationType, declarationId = Some(id)))
  }
}

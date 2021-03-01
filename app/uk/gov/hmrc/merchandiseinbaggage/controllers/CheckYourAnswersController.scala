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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.{declarationNotFoundMessage, incompleteMessage}
import uk.gov.hmrc.merchandiseinbaggage.forms.CheckYourAnswersForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, Declaration}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.core.GoodsEntries
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, PaymentService}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView, CheckYourAnswersExportView, CheckYourAnswersImportView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  calculationService: CalculationService,
  paymentService: PaymentService,
  mibConnector: MibConnector,
  override val repo: DeclarationJourneyRepository,
  importView: CheckYourAnswersImportView,
  exportView: CheckYourAnswersExportView,
  amendImportView: CheckYourAnswersAmendImportView,
  amendExportView: CheckYourAnswersAmendExportView)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.journeyType match {
      case New =>
        request.declarationJourney.declarationIfRequiredAndComplete
          .fold(actionProvider.invalidRequestF(incompleteMessage)) { declaration =>
            request.declarationType match {
              case Import =>
                calculationService.paymentCalculations(declaration.declarationGoods.importGoods).map { calculationResults =>
                  if (calculationResults.totalGbpValue.value > declaration.goodsDestination.threshold.value) {
                    Redirect(routes.GoodsOverThresholdController.onPageLoad())
                  } else Ok(importView(form, declaration, calculationResults))
                }
              case Export =>
                if (declaration.declarationGoods.goods
                      .map(_.purchaseDetails.numericAmount)
                      .sum > declaration.goodsDestination.threshold.inPounds)
                  Future successful Redirect(routes.GoodsOverThresholdController.onPageLoad())
                else Future successful Ok(exportView(form, declaration))
            }
          }
      case Amend =>
        request.declarationJourney.amendmentIfRequiredAndComplete
          .fold(actionProvider.invalidRequestF(incompleteMessage)) { amendment =>
            request.declarationType match {
              case Import =>
                for {
                  calculationResults       <- calculationService.paymentCalculations(amendment.goods.importGoods)
                  maybeOriginalDeclaration <- mibConnector.findDeclaration(request.declarationJourney.declarationId)
                } yield {
                  maybeOriginalDeclaration.fold(actionProvider.invalidRequest(declarationNotFoundMessage)) { originalDeclaration =>
                    originalDeclaration.maybeTotalCalculationResult.fold(actionProvider.invalidRequest(declarationNotFoundMessage)) {
                      originalCalculationResults =>
                        if ((calculationResults.totalGbpValue.value + originalCalculationResults.totalGbpValue.value) > originalDeclaration.goodsDestination.threshold.value) {
                          Redirect(routes.GoodsOverThresholdController.onPageLoad())
                        } else Ok(amendImportView(form, amendment, calculationResults))
                    }
                  }
                }
              case Export =>
                mibConnector.findDeclaration(request.declarationJourney.declarationId).map { maybeOriginalDeclaration =>
                  maybeOriginalDeclaration.fold(actionProvider.invalidRequest(declarationNotFoundMessage)) { originalDeclaration =>
                    val originalGbpValue = originalDeclaration.declarationGoods.goods.map(_.purchaseDetails.numericAmount).sum
                    val amendGbpValue = amendment.goods.goods.map(_.purchaseDetails.numericAmount).sum
                    if ((originalGbpValue + amendGbpValue) > originalDeclaration.goodsDestination.threshold.inPounds) {
                      Redirect(routes.GoodsOverThresholdController.onPageLoad())
                    } else Ok(amendExportView(form, amendment))
                  }
                }
            }
          }
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.journeyType match {
      case New =>
        request.declarationJourney.declarationIfRequiredAndComplete
          .fold(actionProvider.invalidRequestF(incompleteMessage))(declaration =>
            handleNewDeclaration(declaration.copy(lang = messages.lang.code)))
      case Amend =>
        request.declarationJourney.amendmentIfRequiredAndComplete
          .fold(actionProvider.invalidRequestF(incompleteMessage))(amendment => handleAmendDeclaration(amendment))
    }
  }

  val addMoreGoods: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    val updatedGoodsEntries: GoodsEntries = request.declarationJourney.goodsEntries.addEmptyIfNecessary()

    repo.upsert(request.declarationJourney.copy(goodsEntries = updatedGoodsEntries)).map { _ =>
      Redirect(routes.GoodsTypeQuantityController.onPageLoad(updatedGoodsEntries.entries.size))
    }
  }

  private def handleNewDeclaration(declaration: Declaration)(implicit request: DeclarationJourneyRequest[AnyContent]): Future[Result] =
    declaration.declarationType match {
      case Export =>
        continueExportDeclaration(declaration)
      case Import =>
        continueImportDeclaration(declaration)
    }

  private def handleAmendDeclaration(amendment: Amendment)(implicit request: DeclarationJourneyRequest[AnyContent]): Future[Result] =
    mibConnector.findDeclaration(request.declarationJourney.declarationId).flatMap { maybeOriginalDeclaration =>
      maybeOriginalDeclaration.fold(actionProvider.invalidRequestF(declarationNotFoundMessage)) { originalDeclaration =>
        originalDeclaration.declarationType match {
          case Import =>
            calculationService
              .paymentCalculations(amendment.goods.importGoods)
              .map { taxDue =>
                val updatedAmendment = amendment.copy(maybeTotalCalculationResult = Some(taxDue.totalCalculationResult))
                val updatedAmendments = originalDeclaration.amendments :+ updatedAmendment
                val updatedDeclaration = originalDeclaration.copy(
                  amendments = updatedAmendments,
                  lang = messages.lang.code
                )

                mibConnector.persistDeclaration(updatedDeclaration).flatMap { _ =>
                  paymentService.sendPaymentRequest(originalDeclaration.mibReference, taxDue).map { redirectUrl =>
                    Redirect(redirectUrl)
                  }
                }
              }
              .flatten
          case Export =>
            val updatedAmendments = originalDeclaration.amendments :+ amendment
            val updatedDeclaration = originalDeclaration.copy(
              amendments = updatedAmendments,
              lang = messages.lang.code
            )

            mibConnector.persistDeclaration(updatedDeclaration).map(_ => Redirect(routes.DeclarationConfirmationController.onPageLoad()))
        }
      }
    }

  private def continueExportDeclaration(declaration: Declaration)(implicit request: DeclarationJourneyRequest[AnyContent]) =
    mibConnector.persistDeclaration(declaration).map(_ => Redirect(routes.DeclarationConfirmationController.onPageLoad()))

  private def continueImportDeclaration(declaration: Declaration)(implicit request: DeclarationJourneyRequest[AnyContent]): Future[Result] =
    for {
      taxDue      <- calculationService.paymentCalculations(declaration.declarationGoods.importGoods)
      _           <- mibConnector.persistDeclaration(declaration.copy(maybeTotalCalculationResult = Some(taxDue.totalCalculationResult)))
      redirectUrl <- paymentService.sendPaymentRequest(declaration.mibReference, taxDue)

    } yield Redirect(redirectUrl)
}

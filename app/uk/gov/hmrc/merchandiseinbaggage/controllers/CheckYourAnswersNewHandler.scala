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

import com.google.inject.{Inject, Singleton}
import play.api.i18n.Messages
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.forms.CheckYourAnswersForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, PaymentService}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersExportView, CheckYourAnswersImportView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersNewHandler @Inject()(
  calculationService: CalculationService,
  paymentService: PaymentService,
  mibConnector: MibConnector,
  importView: CheckYourAnswersImportView,
  exportView: CheckYourAnswersExportView
)(implicit val ec: ExecutionContext, val appConfig: AppConfig) {

  def onPageLoad(declaration: Declaration)(implicit hc: HeaderCarrier, request: Request[_], messages: Messages): Future[Result] =
    declaration.declarationType match {
      case Import => onPageLoadImport(declaration)
      case Export => onPageLoadExport(declaration)
    }

  private def onPageLoadImport(
    declaration: Declaration)(implicit hc: HeaderCarrier, request: Request[_], messages: Messages): Future[Result] =
    calculationService.paymentCalculations(declaration.declarationGoods.importGoods).map { calculationResults =>
      if (calculationResults.totalGbpValue.value > declaration.goodsDestination.threshold.value) {
        Redirect(routes.GoodsOverThresholdController.onPageLoad())
      } else Ok(importView(form, declaration, calculationResults))
    }

  private def onPageLoadExport(declaration: Declaration)(implicit request: Request[_], messages: Messages): Future[Result] =
    if (declaration.declarationGoods.goods
          .map(_.purchaseDetails.numericAmount)
          .sum > declaration.goodsDestination.threshold.inPounds)
      Future successful Redirect(routes.GoodsOverThresholdController.onPageLoad())
    else Future successful Ok(exportView(form, declaration))

  def onSubmit(declaration: Declaration)(implicit hc: HeaderCarrier): Future[Result] =
    declaration.declarationType match {
      case Export =>
        persistAndRedirect(declaration)
      case Import =>
        persistAndRedirectToPayments(declaration)
    }

  private def persistAndRedirect(declaration: Declaration)(implicit hc: HeaderCarrier) =
    mibConnector.persistDeclaration(declaration).map(_ => Redirect(routes.DeclarationConfirmationController.onPageLoad()))

  private def persistAndRedirectToPayments(declaration: Declaration)(implicit hc: HeaderCarrier): Future[Result] =
    for {
      taxDue      <- calculationService.paymentCalculations(declaration.declarationGoods.importGoods)
      _           <- mibConnector.persistDeclaration(declaration.copy(maybeTotalCalculationResult = Some(taxDue.totalCalculationResult)))
      redirectUrl <- paymentService.sendPaymentRequest(declaration.mibReference, None, taxDue)

    } yield Redirect(redirectUrl)
}

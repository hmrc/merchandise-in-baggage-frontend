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
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.declarationNotFoundMessage
import uk.gov.hmrc.merchandiseinbaggage.forms.CheckYourAnswersForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, DeclarationId, DeclarationType}
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, PaymentService}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersAmendHandler @Inject()(
  actionProvider: DeclarationJourneyActionProvider,
  paymentService: PaymentService,
  calculationService: CalculationService,
  mibConnector: MibConnector,
  amendImportView: CheckYourAnswersAmendImportView,
  amendExportView: CheckYourAnswersAmendExportView)(implicit val ec: ExecutionContext, val appConfig: AppConfig) {

  def onPageLoad(declarationType: DeclarationType, amendment: Amendment, declarationId: DeclarationId)(
    implicit hc: HeaderCarrier,
    request: Request[_],
    messages: Messages): Future[Result] =
    declarationType match {
      case Import => onPageLoadImport(amendment, declarationId)
      case Export => onPageLoadExport(amendment, declarationId)
    }

  private def onPageLoadImport(
    amendment: Amendment,
    declarationId: DeclarationId)(implicit hc: HeaderCarrier, request: Request[_], messages: Messages): Future[Result] =
    for {
      calculationResults       <- calculationService.paymentCalculations(amendment.goods.importGoods)
      maybeOriginalDeclaration <- mibConnector.findDeclaration(declarationId)
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

  private def onPageLoadExport(
    amendment: Amendment,
    declarationId: DeclarationId)(implicit hc: HeaderCarrier, request: Request[_], messages: Messages): Future[Result] =
    mibConnector.findDeclaration(declarationId).map { maybeOriginalDeclaration =>
      maybeOriginalDeclaration.fold(actionProvider.invalidRequest(declarationNotFoundMessage)) { originalDeclaration =>
        val originalGbpValue = originalDeclaration.declarationGoods.goods.map(_.purchaseDetails.numericAmount).sum
        val amendGbpValue = amendment.goods.goods.map(_.purchaseDetails.numericAmount).sum
        if ((originalGbpValue + amendGbpValue) > originalDeclaration.goodsDestination.threshold.inPounds) {
          Redirect(routes.GoodsOverThresholdController.onPageLoad())
        } else Ok(amendExportView(form, amendment))
      }
    }
}

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
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.forms.CheckYourAnswersForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{OverThreshold, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.service.{Auditor, CalculationService, PaymentService}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersExportView, CheckYourAnswersImportView}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersNewHandler @Inject()(
  calculationService: CalculationService,
  paymentService: PaymentService,
  mibConnector: MibConnector,
  val auditConnector: AuditConnector,
  val messagesApi: MessagesApi,
  importView: CheckYourAnswersImportView,
  exportView: CheckYourAnswersExportView
)(implicit val ec: ExecutionContext, val appConfig: AppConfig)
    extends Auditor {

  def onPageLoad(
    declaration: Declaration,
    isAgent: YesNo)(implicit hc: HeaderCarrier, request: Request[_], messages: Messages): Future[Result] =
    calculationService.paymentCalculations(declaration.declarationGoods.goods, declaration.goodsDestination).map { calculationResponse =>
      (calculationResponse.thresholdCheck, declaration.declarationType) match {
        case (OverThreshold, _)        => Redirect(routes.GoodsOverThresholdController.onPageLoad())
        case (WithinThreshold, Import) => Ok(importView(form, declaration, calculationResponse.results, isAgent))
        case (WithinThreshold, Export) => Ok(exportView(form, declaration, isAgent))
      }
    }

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
      calculationResponse <- calculationService.paymentCalculations(declaration.declarationGoods.goods, declaration.goodsDestination)
      declarationWithCalculationResponse = declaration.copy(
        maybeTotalCalculationResult = Some(calculationResponse.results.totalCalculationResult))
      _           <- mibConnector.persistDeclaration(declarationWithCalculationResponse)
      redirectUrl <- paymentService.sendPaymentRequest(declaration.mibReference, None, calculationResponse.results)
      _           <- auditDeclaration(declarationWithCalculationResponse)
    } yield Redirect(redirectUrl)
}

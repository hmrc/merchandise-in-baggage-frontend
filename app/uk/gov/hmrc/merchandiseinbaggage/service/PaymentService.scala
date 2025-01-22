/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.service

import com.google.inject.Singleton
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.PaymentConnector
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResults
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.PayApiRequest
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentService @Inject() (
  connector: PaymentConnector,
  val auditConnector: AuditConnector
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends Auditor {

  def sendPaymentRequest(declaration: Declaration, amendmentRef: Option[Int], paymentCalcs: CalculationResults)(implicit
    hc: HeaderCarrier
  ): Future[String] =
    if (paymentCalcs.totalTaxDue.value == 0) {
      Future.successful(routes.DeclarationConfirmationController.onPageLoad.url)
    } else {
      auditDeclaration(declaration)
      connector
        .sendPaymentRequest(
          PayApiRequest(
            declaration.mibReference,
            paymentCalcs.totalTaxDue,
            paymentCalcs.totalVatDue,
            paymentCalcs.totalDutyDue,
            appConfig.paymentsReturnUrl,
            appConfig.paymentsBackUrl,
            amendmentRef
          )
        )
        .map(_.nextUrl.value)
    }
}

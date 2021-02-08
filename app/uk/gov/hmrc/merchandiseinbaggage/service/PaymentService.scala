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

package uk.gov.hmrc.merchandiseinbaggage.service

import com.google.inject.Singleton
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.PaymentConnector
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes
import uk.gov.hmrc.merchandiseinbaggage.model.api.MibReference
import javax.inject.Inject
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResults
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.PayApiRequest

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

@Singleton
class PaymentService @Inject()(connector: PaymentConnector)(implicit ec: ExecutionContext, appConfig: AppConfig) {

  def sendPaymentRequest(mibRef: MibReference, paymentCalcs: CalculationResults)(implicit hc: HeaderCarrier): Future[String] =
    if (paymentCalcs.totalTaxDue.value == 0) {
      Future.successful(routes.DeclarationConfirmationController.onPageLoad().url)
    } else {
      connector
        .sendPaymentRequest(
          PayApiRequest(
            mibRef,
            paymentCalcs.totalTaxDue,
            paymentCalcs.totalVatDue,
            paymentCalcs.totalDutyDue,
            appConfig.paymentsReturnUrl,
            appConfig.paymentsBackUrl
          )
        )
        .map(_.nextUrl.value)
    }
}

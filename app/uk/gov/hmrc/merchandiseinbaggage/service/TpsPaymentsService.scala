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

package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.TpsPaymentsBackendConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResults
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.PayApiResponse
import uk.gov.hmrc.merchandiseinbaggage.model.api.tpspayments.TpsPaymentsRequest
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsPaymentsService @Inject() (
  connector: TpsPaymentsBackendConnector,
  val auditConnector: AuditConnector
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends Auditor {

  def createTpsPayments(
    amendmentRef: Option[Int],
    declaration: Declaration,
    paymentDue: CalculationResults
  )(implicit hc: HeaderCarrier): Future[PayApiResponse] =
    for {
      _           <- auditDeclaration(declaration)
      tpsResponse <- connector.tpsPayments(buildTpsRequest(amendmentRef, declaration, paymentDue))
    } yield tpsResponse

  private[service] def buildTpsRequest(
    amendmentRef: Option[Int],
    declaration: Declaration,
    paymentDue: CalculationResults
  ) =
    TpsPaymentsRequest(
      mibReference = declaration.mibReference.value,
      customerName = declaration.nameOfPersonCarryingTheGoods.toString,
      amount = paymentDue.totalTaxDue.inPounds,
      amendmentReference = amendmentRef,
      totalVatDue = paymentDue.totalVatDue.inPounds,
      totalDutyDue = paymentDue.totalDutyDue.inPounds,
      backUrl = appConfig.tpsNavigation.back,
      resetUrl = appConfig.tpsNavigation.reset,
      finishUrl = appConfig.tpsNavigation.finish
    )
}

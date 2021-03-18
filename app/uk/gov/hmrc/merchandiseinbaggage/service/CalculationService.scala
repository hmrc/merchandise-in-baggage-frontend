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

import cats.data.OptionT
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResult, CalculationResults}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationId, ImportGoods}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{AmendCalculationResult, DeclarationJourney}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CalculationService @Inject()(mibConnector: MibConnector)(implicit ec: ExecutionContext) {
  private val logger = Logger("CalculationService")

  def paymentCalculations(importGoods: Seq[ImportGoods])(implicit hc: HeaderCarrier): Future[CalculationResults] =
    mibConnector.calculatePayments(importGoods.map(_.calculationRequest)).map(withLogging)

  def amendDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier): Future[DeclarationId] =
    mibConnector.amendDeclaration(declaration)

  def findDeclaration(declarationId: DeclarationId)(implicit hc: HeaderCarrier): Future[Option[Declaration]] =
    mibConnector.findDeclaration(declarationId)

  def isAmendPlusOriginalOverThreshold(declarationJourney: DeclarationJourney)(
    implicit hc: HeaderCarrier): OptionT[Future, AmendCalculationResult] =
    for {
      calculationResults         <- amendCalculation(declarationJourney)
      originalDeclaration        <- OptionT(mibConnector.findDeclaration(declarationJourney.declarationId))
      originalCalculationResults <- OptionT.fromOption[Future](originalDeclaration.maybeTotalCalculationResult)
      totalGbpAmount = calculationResults.totalGbpValue.value + originalCalculationResults.totalGbpValue.value
    } yield AmendCalculationResult(totalGbpAmount > originalDeclaration.goodsDestination.threshold.value, calculationResults)

  private def amendCalculation(declarationJourney: DeclarationJourney)(implicit hc: HeaderCarrier): OptionT[Future, CalculationResults] =
    for {
      amendments         <- OptionT.fromOption[Future](declarationJourney.amendmentIfRequiredAndComplete)
      calculationResults <- OptionT.liftF(paymentCalculations(amendments.goods.importGoods))
    } yield calculationResults

  private def withLogging(calculationResults: Seq[CalculationResult]): CalculationResults = {
    calculationResults.foreach(result => logger.info(s"Payment calculation for good [${result.goods}] gave result [$result]"))
    CalculationResults(calculationResults)
  }
}

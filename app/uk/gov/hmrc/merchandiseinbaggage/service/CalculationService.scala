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
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResponse
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationId, Goods, GoodsDestination}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CalculationService @Inject()(mibConnector: MibConnector)(implicit ec: ExecutionContext) {
  private val logger = Logger("CalculationService")

  def paymentCalculations(goods: Seq[Goods], destination: GoodsDestination)(implicit hc: HeaderCarrier): Future[CalculationResponse] =
    mibConnector.calculatePayments(goods.map(_.calculationRequest(destination))).map(withLogging)

  def amendDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier): Future[DeclarationId] =
    mibConnector.amendDeclaration(declaration)

  def findDeclaration(declarationId: DeclarationId)(implicit hc: HeaderCarrier): Future[Option[Declaration]] =
    mibConnector.findDeclaration(declarationId)

  //TODO both logic to be moved to BE
  def isAmendPlusOriginalOverThresholdImport(declarationJourney: DeclarationJourney)(
    implicit hc: HeaderCarrier): OptionT[Future, CalculationResponse] =
    for {
      amendments          <- OptionT.fromOption[Future](declarationJourney.amendmentIfRequiredAndComplete)
      destination         <- OptionT.fromOption[Future](declarationJourney.maybeGoodsDestination)
      originalDeclaration <- OptionT(mibConnector.findDeclaration(declarationJourney.declarationId))
      totalGoods = amendments.goods.goods ++ originalDeclaration.declarationGoods.goods
      calculationResponse <- OptionT.liftF(paymentCalculations(totalGoods, destination))
    } yield calculationResponse

  def isAmendPlusOriginalOverThresholdExport(declarationJourney: DeclarationJourney)(
    implicit hc: HeaderCarrier): OptionT[Future, CalculationResponse] =
    for {
      amendments          <- OptionT.fromOption[Future](declarationJourney.amendmentIfRequiredAndComplete)
      originalDeclaration <- OptionT(mibConnector.findDeclaration(declarationJourney.declarationId))
      totalGoods = amendments.goods.goods ++ originalDeclaration.declarationGoods.goods
      calculationResponse <- OptionT.liftF(
                              mibConnector.calculatePayments(totalGoods.map(_.calculationRequest(originalDeclaration.goodsDestination))))
    } yield calculationResponse

  private def withLogging(response: CalculationResponse): CalculationResponse = {
    response.results.calculationResults.foreach(result =>
      logger.info(s"Payment calculation for good [${result.goods}] gave result [$result]"))
    response
  }
}

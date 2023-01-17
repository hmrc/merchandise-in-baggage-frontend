/*
 * Copyright 2023 HM Revenue & Customs
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
import cats.instances.future._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationAmendRequest, CalculationResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries, ThresholdAllowance}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.viewmodels.DeclarationView._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MibService @Inject()(mibConnector: MibConnector)(implicit ec: ExecutionContext) {
  private val logger = Logger("MibService")

  def paymentCalculations(goods: Seq[Goods], destination: GoodsDestination)(implicit hc: HeaderCarrier): Future[CalculationResponse] =
    mibConnector.calculatePayments(goods.map(_.calculationRequest(destination))).map(withLogging)

  def amendDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier): Future[DeclarationId] =
    mibConnector.amendDeclaration(declaration)

  def findDeclaration(declarationId: DeclarationId)(implicit hc: HeaderCarrier): Future[Option[Declaration]] =
    mibConnector.findDeclaration(declarationId)

  def amendPlusOriginalCalculations(declarationJourney: DeclarationJourney)(
    implicit hc: HeaderCarrier): OptionT[Future, CalculationResponse] = {
    import declarationJourney._
    OptionT.liftF(
      mibConnector.calculatePaymentsAmendPlusExisting(
        CalculationAmendRequest(
          amendmentIfRequiredAndComplete,
          maybeGoodsDestination,
          declarationId
        )))
  }

  def thresholdAllowance(
    maybeGoodsDestination: Option[GoodsDestination],
    goodsEntries: GoodsEntries,
    journeyType: JourneyType,
    declarationId: DeclarationId)(implicit hc: HeaderCarrier): OptionT[Future, ThresholdAllowance] =
    for {
      declarationGoods <- OptionT.fromOption(goodsEntries.declarationGoodsIfComplete)
      destination      <- OptionT.fromOption(maybeGoodsDestination)
      totalGoods       <- addGoods(journeyType, declarationId, declarationGoods.goods)
      calculation      <- OptionT.liftF(paymentCalculations(totalGoods, destination))
    } yield ThresholdAllowance(DeclarationGoods(declarationGoods.goods), DeclarationGoods(totalGoods), calculation, destination)

  def thresholdAllowance(declaration: Declaration)(implicit hc: HeaderCarrier): Future[ThresholdAllowance] = {
    import declaration._
    val totalGoods = allGoods(declaration)
    paymentCalculations(totalGoods, goodsDestination).map(calculation =>
      ThresholdAllowance(DeclarationGoods(declarationGoods.goods), DeclarationGoods(totalGoods), calculation, goodsDestination))
  }

  private[service] def addGoods(journeyType: JourneyType, declarationId: DeclarationId, goods: Seq[Goods])(
    implicit hc: HeaderCarrier): OptionT[Future, Seq[Goods]] =
    journeyType match {
      case New => OptionT.pure(goods)
      case Amend =>
        OptionT(findDeclaration(declarationId)).map { declaration =>
          goods ++ allGoods(declaration)
        }
    }

  private def withLogging(response: CalculationResponse): CalculationResponse = {
    response.results.calculationResults.foreach(result =>
      logger.info(s"Payment calculation for good [${result.goods}] gave result [$result]"))
    response
  }
}

/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.calculation.CalculationResult
import uk.gov.hmrc.merchandiseinbaggage.model.core.{AmountInPence, DeclarationGoods, PaymentCalculation, PaymentCalculations}
import uk.gov.hmrc.merchandiseinbaggage.model.currencyconversion.ConversionRatePeriod

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode.HALF_UP

@Singleton
class CalculationService @Inject()(connector: CurrencyConversionConnector)(implicit ec: ExecutionContext) {
  private val logger = Logger("CalculationService")

  def paymentCalculation(declarationGoods: DeclarationGoods)(implicit hc: HeaderCarrier): Future[PaymentCalculations] =
    Future.traverse(declarationGoods.goods) { good =>
      val code = good.purchaseDetails.currency.code

      val futureRate: Future[BigDecimal] =
        if(code == "GBP") Future.successful(BigDecimal(1))
        else connector.getConversionRate(code).map(_.find(_.currencyCode == code).fold(BigDecimal(0))(_.rate))

      futureRate.map { rate =>
        val converted: BigDecimal = (good.purchaseDetails.numericAmount / rate).setScale(2, HALF_UP)

        val duty = (converted * 0.033).setScale(2, HALF_UP)

        val vatRate = BigDecimal(good.goodsVatRate.value / 100.0)

        val vat = ((converted + duty) * vatRate).setScale(2, HALF_UP)

        val result = CalculationResult(
          AmountInPence((converted * 100).toLong),
          AmountInPence((duty * 100).toLong),
          AmountInPence((vat * 100).toLong)
        )

        logger.info(s"Payment calculation for good [$good] with fx rate [$rate] vat rate [$vatRate] gave result [$result]")

        PaymentCalculation(good, result)
      }
    }.map(PaymentCalculations.apply)

  def getConversionRates(declarationGoods: DeclarationGoods)(implicit hc: HeaderCarrier): Future[Seq[ConversionRatePeriod]] = {
    val codes = declarationGoods.goods
      .filterNot(_.purchaseDetails.currency.code == "GBP")
      .map(_.purchaseDetails.currency.code).distinct.mkString("&cc=")

    if(codes.isEmpty)
      Future(Seq.empty)
    else
      connector.getConversionRate(codes)
  }
}

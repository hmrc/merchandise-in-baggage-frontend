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

package uk.gov.hmrc.merchandiseinbaggagefrontend.service

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggagefrontend.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.calculation.CalculationResult
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{AmountInPence, DeclarationGoods, PaymentCalculation, PaymentCalculations}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.ConversionRatePeriod

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CalculationService @Inject()(connector: CurrencyConversionConnector)(implicit ec: ExecutionContext) {

  def paymentCalculation(declarationGoods: DeclarationGoods)(implicit hc: HeaderCarrier): Future[PaymentCalculations] =
    Future.traverse(declarationGoods.goods) { good =>
      val code = good.purchaseDetails.currency.currencyCode
      connector.getConversionRate(code).map { rates =>
        val rounding = BigDecimal.RoundingMode.HALF_UP

        val rate: BigDecimal = rates.find(_.currencyCode == code).fold(BigDecimal(0))(_.rate)

        val converted: BigDecimal = (good.purchaseDetails.numericAmount / rate).setScale(2, rounding)

        val duty = (converted * 0.033).setScale(2, rounding)

        val vatRate = BigDecimal(good.goodsVatRate.value / 100.0)

        val vat = ((converted + duty) * vatRate).setScale(2, rounding)

        val result = CalculationResult(
          AmountInPence((converted * 100).toLong),
          AmountInPence((duty * 100).toLong),
          AmountInPence((vat * 100).toLong)
        )

        PaymentCalculation(good, result)
      }
    }.map(PaymentCalculations)

  def getConversionRates(declarationGoods: DeclarationGoods)(implicit hc: HeaderCarrier): Future[Seq[ConversionRatePeriod]] = {
    val codes = declarationGoods.goods.map(_.purchaseDetails.currency.currencyCode).distinct.mkString("&cc=")

    connector.getConversionRate(codes)
  }

}

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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.calculation.CalculationResult
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.stubs.CurrencyConversionStub.givenCurrencyIsFound
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import com.softwaremill.quicklens._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TaxCalculation {
  this: BasePageSpec[_] =>

  @deprecated("Using payment BE Calculation we do not need to stub currency conversion if done in the BE")
  def givenADeclarationWithTaxDue(
    declarationJourney: DeclarationJourney,
    overThreshold: Option[Int] = Some(0)): Future[PaymentCalculations] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    def givenCurrenciesAreFound(): Unit =
      declarationJourney.goodsEntries.declarationGoodsIfComplete.get.goods
        .map(_.purchaseDetails.currency)
        .toSet
        .foreach { ccy: Currency =>
          givenCurrencyIsFound(ccy.code, wireMockServer)
          ()
        }

    givenADeclarationJourney(declarationJourney)
    givenCurrenciesAreFound()

    val calculationResult = CalculationResult(AmountInPence(7834), AmountInPence(0), AmountInPence(1567))
    val resTwo = CalculationResult(AmountInPence(7834), AmountInPence(0), AmountInPence(1567))
    val calculations = overThreshold.fold(List(calculationResult, resTwo))(over =>
      List(calculationResult, resTwo).map(_.modify(_.gbpAmount).using(x => AmountInPence(x.value + over))))

    val goods = declarationJourney.goodsEntries.declarationGoodsIfComplete.get.goods
    val results: Seq[PaymentCalculation] = goods.zipWithIndex.map {
      case (g, idx) => PaymentCalculation(g, calculations(idx))
    }

    results.foreach { r: PaymentCalculation =>
      givenAPaymentCalculation(wireMockServer, r.calculationResult)
    }

    givenADeclarationJourney(declarationJourney)
    Future(PaymentCalculations(results))
  }
}

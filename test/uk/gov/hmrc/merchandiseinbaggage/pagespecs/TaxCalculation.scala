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
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Currency, DeclarationJourney, PaymentCalculations}
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.stubs.CurrencyConversionStub.givenCurrencyIsFound

import scala.concurrent.Future

trait TaxCalculation {
  this: BasePageSpec[_] =>

  private lazy val calculationService = injector.instanceOf[CalculationService]

  def givenADeclarationWithTaxDue(declarationJourney: DeclarationJourney): Future[PaymentCalculations] = {
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

    calculationService.paymentCalculation(declarationJourney.goodsEntries.declarationGoodsIfComplete.get)
  }
}

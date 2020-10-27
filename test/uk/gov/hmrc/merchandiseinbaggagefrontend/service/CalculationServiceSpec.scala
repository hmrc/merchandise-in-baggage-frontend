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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.calculation.CalculationResult
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.Currency
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.CurrencyConversionStub._
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpecWithApplication, WireMockSupport, CoreTestData}

class CalculationServiceSpec extends BaseSpecWithApplication with WireMockSupport with CoreTestData {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val service = injector.instanceOf[CalculationService]

  "taxCalculation" must {
    "take a sequence of DeclarationGoods and return TaxCalculations" in {

      givenCurrencyIsFound("EUR", wireMockServer)

      val good = Goods(
        CategoryQuantityOfGoods("test good", "123"),
        GoodsVatRates.Twenty,
        "France",
        PurchaseDetails("100", Currency("Eurozone", "Euro", "EUR")),
        "123456"
      )

      val result: PaymentCalculations = service.paymentCalculation(DeclarationGoods(good)).futureValue

      val expected: PaymentCalculations = PaymentCalculations(
        Seq(
          PaymentCalculation(
            good,
            CalculationResult(
              AmountInPence(7835),
              AmountInPence(259),
              AmountInPence(1619)
            )
          )
        )
      )

      result mustBe expected
    }
  }

}

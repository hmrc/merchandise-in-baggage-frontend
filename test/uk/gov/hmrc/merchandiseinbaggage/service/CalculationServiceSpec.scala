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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.PurchaseDetails
import uk.gov.hmrc.merchandiseinbaggage.model.calculation.CalculationResult
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.stubs.CurrencyConversionStub._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData, WireMockSupport}

class CalculationServiceSpec extends BaseSpecWithApplication with WireMockSupport with CoreTestData {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val service = injector.instanceOf[CalculationService]

  "paymentCalculation" must {
    "take a sequence of DeclarationGoods and return PaymentCalculations" in {

      givenCurrencyIsFound("EUR", wireMockServer)

      val good = Goods(
        CategoryQuantityOfGoods("test good", "123"),
        GoodsVatRates.Twenty,
        Country("FR", "title.france", "FR", isEu = true, Nil),
        PurchaseDetails("100", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European")))
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

  "getConversionRates" must {
    "ignore GBP" in {
      val goods = Goods(
        CategoryQuantityOfGoods("test good", "123"),
        GoodsVatRates.Twenty,
        Country("FR", "title.france", "FR", isEu = true, Nil),
        PurchaseDetails(
          "100",
          Currency(
            "GBP",
            "title.british_pounds_gbp",
            None,
            List("England", "Scotland", "Wales", "Northern Ireland", "British", "sterling", "pound", "GB")))
      )

      service.getConversionRates(DeclarationGoods(goods)).futureValue mustBe Seq.empty
    }
  }

}

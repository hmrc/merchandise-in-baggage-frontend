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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration

import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpec, CoreTestData}

class DeclarationSpec extends BaseSpec with CoreTestData{
  "Goods" should {
    "be derivable from a completed Goods Entry" in {
      Goods(completedGoodsEntry) mustBe
        Goods(
          completedGoodsEntry.maybeCategoryQuantityOfGoods.get,
          completedGoodsEntry.maybeGoodsVatRate.get,
          completedGoodsEntry.maybeCountryOfPurchase.get,
          completedGoodsEntry.maybePriceOfGoods.get,
          completedGoodsEntry.maybeInvoiceNumber.get,
          completedGoodsEntry.maybeTaxDue.get
        )
    }
  }

  "DeclarationJourney" should {
    "serialise and de-serialise" in {
      parse(toJson(completedDeclarationJourney).toString()).validate[DeclarationJourney].asOpt mustBe Some(completedDeclarationJourney)
    }
  }

  "Declaration" should {
    "be derivable from a completed declaration journey" in {
      declaration mustBe
        Declaration(
          sessionId,
          completedDeclarationJourney.goodsEntries.map(entry => Goods(entry)),
          completedDeclarationJourney.maybeName.get,
          completedDeclarationJourney.maybeAddress.get,
          completedDeclarationJourney.maybeEori.get,
          completedDeclarationJourney.maybeJourneyDetails.get
        )
    }

    "serialise and de-serialise" in {
      parse(toJson(declaration).toString()).validate[Declaration].asOpt mustBe Some(declaration)
    }
  }
}

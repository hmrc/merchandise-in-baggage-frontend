/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration

import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpec, CoreTestData}

class DeclarationSpec extends BaseSpec with CoreTestData{
  "Goods" should {
    "be derivable from a completed Goods Entry" in {
      Goods(completedGoodsEntry) mustBe
        Goods(
          completedGoodsEntry.typeOfGoods,
          completedGoodsEntry.maybeCountryOfPurchase.get,
          completedGoodsEntry.maybePriceOfGoods.get,
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

/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.model.core

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsVatRates.Twenty
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Country, Currency, PurchaseDetails, YesNoDontKnow}
import uk.gov.hmrc.merchandiseinbaggage.stubs.PayApiStub.{completedExportGoods, completedImportGoods}

class GoodsEntrySpec extends AnyWordSpec with Matchers {

  "ImportGoodsEntry" should {
    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(completedImportGoods) shouldBe Json.obj(
          "maybeCategory"        -> "wine",
          "maybeGoodsVatRate"    -> "Twenty",
          "maybeProducedInEu"    -> "Yes",
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
      }
    }
    "deserialize to JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "maybeCategory"        -> "wine",
          "maybeGoodsVatRate"    -> "Twenty",
          "maybeProducedInEu"    -> "Yes",
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[ImportGoodsEntry] shouldBe JsSuccess(completedImportGoods)
      }
      "maybeGoodsVatRate is not present" in {
        val json = Json.obj(
          "maybeCategory"        -> "wine",
          "maybeProducedInEu"    -> "Yes",
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[ImportGoodsEntry] shouldBe JsSuccess(
          ImportGoodsEntry(
            Some("wine"),
            None,
            Some(YesNoDontKnow.Yes),
            Some(PurchaseDetails("99.99", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))))
          )
        )
      }
      "maybeProducedInEu is not present" in {
        val json = Json.obj(
          "maybeCategory"        -> "wine",
          "maybeGoodsVatRate"    -> "Twenty",
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[ImportGoodsEntry] shouldBe JsSuccess(
          ImportGoodsEntry(
            Some("wine"),
            Some(Twenty),
            None,
            Some(PurchaseDetails("99.99", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))))
          )
        )
      }
      "maybePurchaseDetails is not present" in {
        val json = Json.obj(
          "maybeCategory"     -> "wine",
          "maybeGoodsVatRate" -> "Twenty",
          "maybeProducedInEu" -> "Yes"
        )
        json.validate[ImportGoodsEntry] shouldBe JsSuccess(
          ImportGoodsEntry(Some("wine"), Some(Twenty), Some(YesNoDontKnow.Yes), None)
        )
      }
      "catergory is not present" in {
        val json = Json.obj(
          "maybeGoodsVatRate"    -> "Twenty",
          "maybeProducedInEu"    -> "Yes",
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[ImportGoodsEntry] shouldBe JsSuccess(
          ImportGoodsEntry(
            None,
            Some(Twenty),
            Some(YesNoDontKnow.Yes),
            Some(PurchaseDetails("99.99", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))))
          )
        )
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[ImportGoodsEntry] shouldBe JsSuccess(ImportGoodsEntry(None, None, None, None))
      }
      "an extra field is present" in {
        val json = Json.obj(
          "maybeCategory"        -> "wine",
          "extra"                -> true,
          "maybeGoodsVatRate"    -> "Twenty",
          "maybeProducedInEu"    -> "Yes",
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[ImportGoodsEntry] shouldBe JsSuccess(completedImportGoods)
      }
    }
    "fail to deserialize" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[ImportGoodsEntry] shouldBe a[JsError]
      }
      "there is a type mismatch" in {
        val json = Json.obj(
          "maybeCategory"        -> "wine",
          "maybeGoodsVatRate"    -> true,
          "maybeProducedInEu"    -> "Yes",
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[ImportGoodsEntry] shouldBe a[JsError]
      }
    }
  }
  "ExportGoodsEntry" should {
    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(completedExportGoods) shouldBe Json.obj(
          "maybeCategory"        -> "test good",
          "maybeDestination"     -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          ),
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "GBP",
              "displayName"        -> "title.british_pounds_gbp",
              "currencySynonyms"   -> Json.arr(),
              "valueForConversion" -> "GBP"
            )
          )
        )
      }
    }
    "deserialize to JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "maybeCategory"        -> "test good",
          "maybeDestination"     -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          ),
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "GBP",
              "displayName"        -> "title.british_pounds_gbp",
              "currencySynonyms"   -> Json.arr("abc", "def"),
              "valueForConversion" -> "GBP"
            )
          )
        )
        json.validate[ExportGoodsEntry] shouldBe JsSuccess(
          ExportGoodsEntry(
            Some("test good"),
            Some(Country("FR", "title.france", "FR", true, List())),
            Some(PurchaseDetails("99.99", Currency("GBP", "title.british_pounds_gbp", Some("GBP"), List("abc", "def"))))
          )
        )
      }
      "maybeCatergory is not present" in {
        val json = Json.obj(
          "maybeDestination"     -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          ),
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "GBP",
              "displayName"        -> "title.british_pounds_gbp",
              "currencySynonyms"   -> Json.arr(),
              "valueForConversion" -> "GBP"
            )
          )
        )
        json.validate[ExportGoodsEntry] shouldBe JsSuccess(
          ExportGoodsEntry(
            None,
            Some(Country("FR", "title.france", "FR", true, List())),
            Some(PurchaseDetails("99.99", Currency("GBP", "title.british_pounds_gbp", Some("GBP"), List())))
          )
        )
      }
      "maybeDestination is not present" in {
        val json = Json.obj(
          "maybeCategory"        -> "test good",
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "GBP",
              "displayName"        -> "title.british_pounds_gbp",
              "currencySynonyms"   -> Json.arr(),
              "valueForConversion" -> "GBP"
            )
          )
        )
        json.validate[ExportGoodsEntry] shouldBe JsSuccess(
          ExportGoodsEntry(
            Some("test good"),
            None,
            Some(PurchaseDetails("99.99", Currency("GBP", "title.british_pounds_gbp", Some("GBP"), List())))
          )
        )
      }
      "maybePurchaseDetails is not present" in {
        val json = Json.obj(
          "maybeCategory"    -> "test good",
          "maybeDestination" -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          )
        )
        json.validate[ExportGoodsEntry] shouldBe JsSuccess(
          ExportGoodsEntry(Some("test good"), Some(Country("FR", "title.france", "FR", true, List())), None)
        )
      }
      "an extra field is given" in {
        val json = Json.obj(
          "extra"                -> true,
          "maybeCategory"        -> "test good",
          "maybeDestination"     -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          ),
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "GBP",
              "displayName"        -> "title.british_pounds_gbp",
              "currencySynonyms"   -> Json.arr("abc", "def"),
              "valueForConversion" -> "GBP"
            )
          )
        )
        json.validate[ExportGoodsEntry] shouldBe JsSuccess(
          ExportGoodsEntry(
            Some("test good"),
            Some(Country("FR", "title.france", "FR", true, List())),
            Some(PurchaseDetails("99.99", Currency("GBP", "title.british_pounds_gbp", Some("GBP"), List("abc", "def"))))
          )
        )
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[ExportGoodsEntry] shouldBe JsSuccess(ExportGoodsEntry(None, None, None))
      }
    }
    "fail to deserialize" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[ExportGoodsEntry] shouldBe a[JsError]
      }
      "there is a type mismatch" in {
        val json = Json.obj(
          "maybeCategory"        -> true,
          "maybeDestination"     -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          ),
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "GBP",
              "displayName"        -> "title.british_pounds_gbp",
              "currencySynonyms"   -> Json.arr("abc", "def"),
              "valueForConversion" -> "GBP"
            )
          )
        )
        json.validate[ExportGoodsEntry] shouldBe a[JsError]
      }
    }
  }
  "GoodsEntry"       should {
    "deserialize to import good" when {
      "produced in EU is present" in {
        val json = Json.obj(
          "maybeCategory"        -> "wine",
          "maybeGoodsVatRate"    -> "Twenty",
          "maybeProducedInEu"    -> "Yes",
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[GoodsEntry] shouldBe JsSuccess(completedImportGoods)
      }
    }
    "deserialize to export good" when {
      "destination is present" in {
        val json = Json.obj(
          "maybeCategory"        -> "test good",
          "maybeDestination"     -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          ),
          "maybePurchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "GBP",
              "displayName"        -> "title.british_pounds_gbp",
              "currencySynonyms"   -> Json.arr(),
              "valueForConversion" -> "GBP"
            )
          )
        )
        json.validate[GoodsEntry] shouldBe JsSuccess(completedExportGoods)
      }
      "fail to deserialize" when {
        "producedInEu and destination are not present" in {
          val json = Json.obj(
            "category"        -> "test good",
            "purchaseDetails" -> Json.obj(
              "amount"   -> "99.99",
              "currency" -> Json.obj(
                "code"               -> "GBP",
                "displayName"        -> "title.british_pounds_gbp",
                "currencySynonyms"   -> Json.arr(),
                "valueForConversion" -> "GBP"
              )
            )
          )
          json.validate[GoodsEntry] shouldBe a[JsError]
        }
      }
    }
    "fail to deserialize" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[GoodsEntry] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[GoodsEntry] shouldBe a[JsError]
      }
    }
  }
}

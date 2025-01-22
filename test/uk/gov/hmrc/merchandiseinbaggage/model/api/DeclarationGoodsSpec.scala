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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.stubs.PayApiStub.{aDeclarationGood, aExportGoods, aImportGoods, completedExportGoods, completedImportGoods}

class DeclarationGoodsSpec extends AnyWordSpec with Matchers {

  "ImportGoods"       should {
    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(aImportGoods) shouldBe Json.obj(
          "category"        -> "wine",
          "goodsVatRate"    -> "Twenty",
          "producedInEu"    -> "Yes",
          "purchaseDetails" -> Json.obj(
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
          "category"        -> "wine",
          "goodsVatRate"    -> "Twenty",
          "producedInEu"    -> "Yes",
          "purchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[ImportGoods] shouldBe JsSuccess(aImportGoods)
      }
      "catergory is not defined" in {
        val json = Json.obj(
          "category"        -> "",
          "goodsVatRate"    -> "Twenty",
          "producedInEu"    -> "Yes",
          "purchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[ImportGoods] shouldBe JsSuccess(
          ImportGoods(
            "",
            completedImportGoods.maybeGoodsVatRate.get,
            completedImportGoods.maybeProducedInEu.get,
            completedImportGoods.maybePurchaseDetails.get
          )
        )
      }
      "an extra field is present" in {
        val json = Json.obj(
          "category"        -> "wine",
          "extra"           -> true,
          "goodsVatRate"    -> "Twenty",
          "producedInEu"    -> "Yes",
          "purchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[ImportGoods] shouldBe JsSuccess(aImportGoods)
      }
    }
    "fail to deserialize" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[ImportGoods] shouldBe a[JsError]
      }
      "there is a type mismatch" in {
        val json = Json.obj(
          "category"        -> 1,
          "goodsVatRate"    -> "Twenty",
          "producedInEu"    -> "Yes",
          "purchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[ImportGoods] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[ImportGoods] shouldBe a[JsError]
      }
    }
  }
  "ExportGoods"       should {
    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(aExportGoods) shouldBe Json.obj(
          "category"        -> "test good",
          "destination"     -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          ),
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
      }
    }
    "deserialize to JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "category"        -> "test good",
          "destination"     -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          ),
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
        json.validate[ExportGoods] shouldBe JsSuccess(aExportGoods)
      }
      "catergory is not defined" in {
        val json = Json.obj(
          "category"        -> "",
          "destination"     -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          ),
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
        json.validate[ExportGoods] shouldBe JsSuccess(
          ExportGoods(
            "",
            completedExportGoods.maybeDestination.get,
            completedExportGoods.maybePurchaseDetails.get
          )
        )
      }
      "an extra field is given" in {
        val json = Json.obj(
          "extra"           -> true,
          "category"        -> "test good",
          "destination"     -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          ),
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
        json.validate[ExportGoods] shouldBe JsSuccess(aExportGoods)
      }
    }
    "fail to deserialize" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[ExportGoods] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[ExportGoods] shouldBe a[JsError]
      }
    }
  }
  "Goods"             should {
    "deserialize to import good" when {
      "produced in EU is present" in {
        val json = Json.obj(
          "category"        -> "wine",
          "goodsVatRate"    -> "Twenty",
          "producedInEu"    -> "Yes",
          "purchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
        json.validate[Goods] shouldBe JsSuccess(aImportGoods)
      }
    }
    "deserialize to export good" when {
      "destination is present" in {
        val json = Json.obj(
          "category"        -> "test good",
          "destination"     -> Json.obj(
            "countryName"     -> "title.france",
            "countrySynonyms" -> Json.arr(),
            "code"            -> "FR",
            "alphaTwoCode"    -> "FR",
            "isEu"            -> true
          ),
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
        json.validate[Goods] shouldBe JsSuccess(aExportGoods)
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
          json.validate[Goods] shouldBe a[JsError]
        }
      }
    }
  }
  "Declaration Goods" should {
    "serialize to json" when {
      "all the fields are present" in {
        Json.toJson(aDeclarationGood) shouldBe Json.obj(
          "goods" -> Json.arr(
            Json.obj(
              "category"        -> "test good",
              "goodsVatRate"    -> "Twenty",
              "producedInEu"    -> "Yes",
              "purchaseDetails" -> Json.obj(
                "amount"   -> "199.99",
                "currency" -> Json.obj(
                  "code"               -> "EUR",
                  "displayName"        -> "title.euro_eur",
                  "currencySynonyms"   -> Json.arr("Europe", "European"),
                  "valueForConversion" -> "EUR"
                )
              )
            )
          )
        )
      }
    }
    "deserialize from json" when {
      "all fields are valid" in {
        val json = Json.obj(
          "goods" -> Json.arr(
            Json.obj(
              "category"        -> "test good",
              "goodsVatRate"    -> "Twenty",
              "producedInEu"    -> "Yes",
              "purchaseDetails" -> Json.obj(
                "amount"   -> "199.99",
                "currency" -> Json.obj(
                  "code"               -> "EUR",
                  "displayName"        -> "title.euro_eur",
                  "currencySynonyms"   -> Json.arr("Europe", "European"),
                  "valueForConversion" -> "EUR"
                )
              )
            )
          )
        )
        json.validate[DeclarationGoods] shouldBe JsSuccess(aDeclarationGood)
      }
      "an extra field is present" in {
        val json = Json.obj(
          "extra" -> 1,
          "goods" -> Json.arr(
            Json.obj(
              "category"        -> "test good",
              "goodsVatRate"    -> "Twenty",
              "producedInEu"    -> "Yes",
              "purchaseDetails" -> Json.obj(
                "amount"   -> "199.99",
                "currency" -> Json.obj(
                  "code"               -> "EUR",
                  "displayName"        -> "title.euro_eur",
                  "currencySynonyms"   -> Json.arr("Europe", "European"),
                  "valueForConversion" -> "EUR"
                )
              )
            )
          )
        )
        json.validate[DeclarationGoods] shouldBe JsSuccess(aDeclarationGood)
      }
    }
    "fail to deserialize" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[DeclarationGoods] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[DeclarationGoods] shouldBe a[JsError]
      }
    }
  }
}

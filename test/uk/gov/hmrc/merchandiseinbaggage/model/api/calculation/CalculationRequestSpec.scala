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

package uk.gov.hmrc.merchandiseinbaggage.model.api.calculation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.{GoodsDestinations, GoodsVatRates, ImportGoods, YesNoDontKnow}

class CalculationRequestSpec extends AnyWordSpec with Matchers with CoreTestData {

  "CalculationRequest" should {

    "serialize to JSON" when {
      " with all fields defined" in {
        val request = CalculationRequest(
          goods = aImportGoods,
          destination = GoodsDestinations.GreatBritain
        )

        Json.toJson(request) shouldBe Json.obj(
          "goods"       -> Json.obj(
            "category"        -> "wine",
            "goodsVatRate"    -> "Twenty",
            "producedInEu"    -> "Yes",
            "purchaseDetails" -> Json.obj(
              "amount"   -> "99.99",
              "currency" -> Json.obj(
                "code"               -> "EUR",
                "displayName"        -> "title.euro_eur",
                "valueForConversion" -> "EUR",
                "currencySynonyms"   -> Json.arr("Europe", "European")
              )
            )
          ),
          "destination" -> "GreatBritain"
        )
      }
    }

    "deserialize from JSON" when {
      "with all fields defined" in {
        val json = Json.obj(
          "goods"       -> Json.obj(
            "category"        -> "wine",
            "goodsVatRate"    -> "Twenty",
            "producedInEu"    -> "Yes",
            "purchaseDetails" -> Json.obj(
              "amount"   -> "99.99",
              "currency" -> Json.obj(
                "code"               -> "EUR",
                "displayName"        -> "title.euro_eur",
                "valueForConversion" -> "EUR",
                "currencySynonyms"   -> Json.arr("Europe", "European")
              )
            )
          ),
          "destination" -> "GreatBritain"
        )

        json.validate[CalculationRequest] shouldBe JsSuccess(
          CalculationRequest(
            goods = aImportGoods,
            destination = GoodsDestinations.GreatBritain
          )
        )
      }

      "with missing fields" in {
        val json = Json.obj(
          "goods" -> Json.obj(
            "category"        -> "Electronics",
            "purchaseDetails" -> Json.obj(
              "amount"   -> "100",
              "currency" -> Json.obj(
                "code"        -> "GBP",
                "displayName" -> "Pound Sterling"
              )
            )
          )
          // Missing "destination"
        )

        json.validate[CalculationRequest] shouldBe a[JsError]
      }

      "with fields of invalid types" in {
        val json = Json.obj(
          "goods"       -> Json.obj(
            "category"        -> "Electronics",
            "purchaseDetails" -> Json.obj(
              "amount"   -> 100,
              "currency" -> Json.obj(
                "code"        -> "GBP",
                "displayName" -> "Pound Sterling"
              )
            )
          ),
          "destination" -> "GreatBritain"
        )

        json.validate[CalculationRequest] shouldBe a[JsError]
      }
    }

    "with support round-trip serialization/deserialization" in {
      val request = CalculationRequest(
        goods = aImportGoods,
        destination = GoodsDestinations.NorthernIreland
      )

      val json = Json.toJson(request)
      json.validate[CalculationRequest] shouldBe JsSuccess(request)
    }

    "serialize and deserialize with empty purchaseDetails" in {
      val request = CalculationRequest(
        goods = ImportGoods(
          category = "test",
          goodsVatRate = GoodsVatRates.Five,
          producedInEu = YesNoDontKnow.Yes,
          purchaseDetails = aPurchaseDetails.copy(amount = "0")
        ),
        destination = GoodsDestinations.GreatBritain
      )

      val json = Json.toJson(request)
      json.validate[CalculationRequest] shouldBe JsSuccess(request)
    }

    "with extra fields" in {
      val json = Json.obj(
        "goods"       -> Json.obj(
          "category"        -> "wine",
          "goodsVatRate"    -> "Twenty",
          "producedInEu"    -> "Yes",
          "purchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "valueForConversion" -> "EUR",
              "currencySynonyms"   -> Json.arr("Europe", "European")
            )
          )
        ),
        "destination" -> "GreatBritain",
        "extraField"  -> "unexpected"
      )

      json.validate[CalculationRequest] shouldBe JsSuccess(
        CalculationRequest(
          goods = aImportGoods,
          destination = GoodsDestinations.GreatBritain
        )
      )
    }
    "with invalid JSON structure" in {
      val json = Json.arr(
        Json.obj("key" -> "value")
      )

      json.validate[CalculationRequest] shouldBe a[JsError]
    }
    "with an empty JSON object" in {
      val json = Json.obj()

      json.validate[CalculationRequest] shouldBe a[JsError]
    }
    "with partial JSON with invalid types" in {
      val json = Json.obj(
        "goods"       -> Json.obj(
          "category"        -> "test",
          "purchaseDetails" -> Json.obj(
            "amount"   -> 100,
            "currency" -> Json.obj(
              "code"        -> "GBP",
              "displayName" -> "Pound Sterling"
            )
          )
        ),
        "destination" -> 123
      )

      json.validate[CalculationRequest] shouldBe a[JsError]
    }

  }
}

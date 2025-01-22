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
import uk.gov.hmrc.merchandiseinbaggage.model.api.AmountInPence

import java.time.LocalDate

class CalculationResultSpec extends AnyWordSpec with Matchers with CoreTestData {

  "CalculationResults" should {
    "Serialize to json" when {
      "all the fields are present" in {
        Json.toJson(aCalculationResult) shouldBe Json.obj(
          "goods"                -> Json.obj(
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
          ),
          "gbpAmount"            -> 10,
          "vat"                  -> 7,
          "duty"                 -> 5,
          "conversionRatePeriod" ->
            Json.obj("startDate" -> LocalDate.now, "endDate" -> LocalDate.now, "currencyCode" -> "EUR", "rate" -> 1.2)
        )
      }
      "vat is 0" in {
        Json.toJson(
          CalculationResult(
            aImportGoods,
            AmountInPence(10L),
            AmountInPence(5),
            AmountInPence(0),
            Some(aConversionRatePeriod)
          )
        ) shouldBe Json.obj(
          "goods"                -> Json.obj(
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
          ),
          "gbpAmount"            -> 10,
          "vat"                  -> 0,
          "duty"                 -> 5,
          "conversionRatePeriod" ->
            Json.obj("startDate" -> LocalDate.now, "endDate" -> LocalDate.now, "currencyCode" -> "EUR", "rate" -> 1.2)
        )
      }
      "Optional fileds are None" in {
        Json.toJson(
          CalculationResult(aImportGoods, AmountInPence(10L), AmountInPence(5), AmountInPence(7), None)
        ) shouldBe Json.obj(
          "goods"     -> Json.obj(
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
          ),
          "gbpAmount" -> 10,
          "vat"       -> 7,
          "duty"      -> 5
        )
      }
    }
    "Deserialize json" when {
      "all the fields are present" in {
        val json = Json.obj(
          "goods"                -> Json.obj(
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
          ),
          "gbpAmount"            -> 10,
          "vat"                  -> 7,
          "duty"                 -> 5,
          "conversionRatePeriod" ->
            Json.obj("startDate" -> LocalDate.now, "endDate" -> LocalDate.now, "currencyCode" -> "EUR", "rate" -> 1.2)
        )
        json.validate[CalculationResult] shouldBe JsSuccess(aCalculationResult)
      }
      "when there is an extra field present" in {
        val json = Json.obj(
          "goods"                -> Json.obj(
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
          ),
          "gbpAmount"            -> 10,
          "vat"                  -> 7,
          "duty"                 -> 5,
          "conversionRatePeriod" ->
            Json.obj("startDate" -> LocalDate.now, "endDate" -> LocalDate.now, "currencyCode" -> "EUR", "rate" -> 1.2)
        )
        json.validate[CalculationResult] shouldBe JsSuccess(aCalculationResult)
      }
      "conversionRatePeriod is not present" in {
        val json = Json.obj(
          "goods"     -> Json.obj(
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
          ),
          "gbpAmount" -> 10,
          "vat"       -> 7,
          "duty"      -> 5
        )
        json.validate[CalculationResult] shouldBe JsSuccess(
          CalculationResult(aImportGoods, AmountInPence(10L), AmountInPence(5), AmountInPence(7), None)
        )
      }
      "when the field type is wrong" in {
        val json = Json.obj(
          "goods"                -> Json.obj(
            "category"        -> "wine",
            "goodsVatRate"    -> "Twenty",
            "producedInEu"    -> "Yes",
            "purchaseDetails" -> Json.obj(
              "amount"   -> 99.99,
              "currency" -> Json.obj(
                "code"               -> "EUR",
                "displayName"        -> "title.euro_eur",
                "currencySynonyms"   -> Json.arr("Europe", "European"),
                "valueForConversion" -> "EUR"
              )
            )
          ),
          "gbpAmount"            -> 10,
          "vat"                  -> 7,
          "duty"                 -> 5,
          "conversionRatePeriod" ->
            Json.obj("startDate" -> LocalDate.now, "endDate" -> LocalDate.now, "currencyCode" -> "EUR", "rate" -> 1.2)
        )
        json.validate[CalculationResult] shouldBe a[JsError]
      }
      "fail to deserialize invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )

        json.validate[CalculationResult] shouldBe a[JsError]
      }
      "fail to deserialize an empty JSON object" in {
        val json = Json.obj()

        json.validate[CalculationResult] shouldBe a[JsError]
      }
    }
  }
}

/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResult, CalculationResults}

class TotalCalculationResultSpec extends AnyWordSpec with Matchers {
  "TotalCalculationResult" should {
    val totalCalculationResult = TotalCalculationResult(
      CalculationResults(
        Seq(
          CalculationResult(
            ImportGoods(
              "sock",
              GoodsVatRates.Twenty,
              YesNoDontKnow.Yes,
              PurchaseDetails(
                "10",
                Currency("GBP", "title.british_pounds_gbp", None, List.empty[String])
              )
            ),
            AmountInPence(0),
            AmountInPence(0),
            AmountInPence(0),
            None
          )
        )
      ),
      AmountInPence(10),
      AmountInPence(0),
      AmountInPence(0),
      AmountInPence(0)
    )
    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(totalCalculationResult) shouldBe Json.obj(
          "totalVatDue"        -> 0,
          "calculationResults" -> Json.obj(
            "calculationResults" -> Json.arr(
              Json.obj(
                "goods"     -> Json.obj(
                  "category"        -> "sock",
                  "goodsVatRate"    -> "Twenty",
                  "producedInEu"    -> "Yes",
                  "purchaseDetails" -> Json.obj(
                    "amount"   -> "10",
                    "currency" -> Json.obj(
                      "code"             -> "GBP",
                      "displayName"      -> "title.british_pounds_gbp",
                      "currencySynonyms" -> Json.arr()
                    )
                  )
                ),
                "gbpAmount" -> 0,
                "duty"      -> 0,
                "vat"       -> 0
              )
            )
          ),
          "totalDutyDue"       -> 0,
          "totalGbpValue"      -> 10,
          "totalTaxDue"        -> 0
        )
      }
    }
    "deserialize to JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "totalVatDue"        -> 0,
          "calculationResults" -> Json.obj(
            "calculationResults" -> Json.arr(
              Json.obj(
                "goods"     -> Json.obj(
                  "category"        -> "sock",
                  "goodsVatRate"    -> "Twenty",
                  "producedInEu"    -> "Yes",
                  "purchaseDetails" -> Json.obj(
                    "amount"   -> "10",
                    "currency" -> Json.obj(
                      "code"             -> "GBP",
                      "displayName"      -> "title.british_pounds_gbp",
                      "currencySynonyms" -> Json.arr()
                    )
                  )
                ),
                "gbpAmount" -> 0,
                "duty"      -> 0,
                "vat"       -> 0
              )
            )
          ),
          "totalDutyDue"       -> 0,
          "totalGbpValue"      -> 10,
          "totalTaxDue"        -> 0
        )
        json.validate[TotalCalculationResult] shouldBe JsSuccess(totalCalculationResult)
      }
      "there is an extra field" in {
        val json = Json.obj(
          "totalVatDue"        -> 0,
          "calculationResults" -> Json.obj(
            "calculationResults" -> Json.arr(
              Json.obj(
                "goods"     -> Json.obj(
                  "category"        -> "sock",
                  "goodsVatRate"    -> "Twenty",
                  "producedInEu"    -> "Yes",
                  "purchaseDetails" -> Json.obj(
                    "amount"   -> "10",
                    "currency" -> Json.obj(
                      "code"             -> "GBP",
                      "displayName"      -> "title.british_pounds_gbp",
                      "currencySynonyms" -> Json.arr()
                    )
                  )
                ),
                "gbpAmount" -> 0,
                "duty"      -> 0,
                "vat"       -> 0
              )
            )
          ),
          "totalDutyDue"       -> 0,
          "totalGbpValue"      -> 10,
          "totalTaxDue"        -> 0,
          "extra"              -> 1
        )
        json.validate[TotalCalculationResult] shouldBe JsSuccess(totalCalculationResult)
      }
    }
    "fail to deserialize to json" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[TotalCalculationResult] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[TotalCalculationResult] shouldBe a[JsError]
      }
    }
  }
}

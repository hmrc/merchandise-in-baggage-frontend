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

package uk.gov.hmrc.merchandiseinbaggage.model.api.calculation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import java.time.LocalDate

class CalculationResponseSpec extends AnyWordSpec with Matchers with CoreTestData {

  "CalculationResponse" should {
    "serialize to JSON" when {
      "all fields are defined" in {
        Json.toJson(aCalculationResponse) shouldBe Json.obj(
          "results"        -> Json.obj(
            "calculationResults" -> Json.arr(
              Json.obj(
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
                "conversionRatePeriod" -> Json
                  .obj("startDate" -> LocalDate.now, "endDate" -> LocalDate.now, "currencyCode" -> "EUR", "rate" -> 1.2)
              )
            )
          ),
          "thresholdCheck" -> "WithinThreshold"
        )
      }
    }
    "deserialize to JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "results"        -> Json.obj(
            "calculationResults" -> Json.arr(
              Json.obj(
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
                "conversionRatePeriod" -> Json
                  .obj("startDate" -> LocalDate.now, "endDate" -> LocalDate.now, "currencyCode" -> "EUR", "rate" -> 1.2)
              )
            )
          ),
          "thresholdCheck" -> "WithinThreshold"
        )
        json.validate[CalculationResponse] shouldBe JsSuccess(aCalculationResponse)
      }
      "an extra fiels defined" in {
        val json = Json.obj(
          "extra"          -> true,
          "results"        -> Json.obj(
            "calculationResults" -> Json.arr(
              Json.obj(
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
                "conversionRatePeriod" -> Json
                  .obj("startDate" -> LocalDate.now, "endDate" -> LocalDate.now, "currencyCode" -> "EUR", "rate" -> 1.2)
              )
            )
          ),
          "thresholdCheck" -> "WithinThreshold"
        )
        json.validate[CalculationResponse] shouldBe JsSuccess(aCalculationResponse)
      }
    }
    "fail to deserialize to json" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[CalculationResponse] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[CalculationResponse] shouldBe a[JsError]
      }
    }
  }
}

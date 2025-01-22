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

import java.time.LocalDate

class ConversionRatePeriodSpec extends AnyWordSpec with Matchers {

  "ConversionRatePeriod" should {
    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(ConversionRatePeriod(LocalDate.now, LocalDate.now, "GBR", 2.3)) shouldBe Json.obj(
          "startDate"    -> LocalDate.now,
          "endDate"      -> LocalDate.now,
          "currencyCode" -> "GBR",
          "rate"         -> 2.3
        )
      }
    }
    "deserialize from JSON" when {
      "the JSON contains a valid Eori value" in {
        val json =
          Json.obj("startDate" -> LocalDate.now, "endDate" -> LocalDate.now, "currencyCode" -> "GBR", "rate" -> 2.3)
        json.validate[ConversionRatePeriod] shouldBe JsSuccess(
          ConversionRatePeriod(LocalDate.now, LocalDate.now, "GBR", 2.3)
        )
      }
      "has an extra field" in {
        val json = Json.obj(
          "startDate"    -> LocalDate.now,
          "endDate"      -> LocalDate.now,
          "currencyCode" -> "GBR",
          "rate"         -> 2.3,
          "extra"        -> true
        )
        json.validate[ConversionRatePeriod] shouldBe JsSuccess(
          ConversionRatePeriod(LocalDate.now, LocalDate.now, "GBR", 2.3)
        )
      }
    }
    "fail to deserialize" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[ConversionRatePeriod] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[ConversionRatePeriod] shouldBe a[JsError]
      }
    }
  }
}

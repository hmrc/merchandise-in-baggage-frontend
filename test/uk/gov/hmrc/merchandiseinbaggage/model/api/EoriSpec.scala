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

class EoriSpec extends AnyWordSpec with Matchers {
  val validEori = Eori("GB123456789000")

  "Eori" should {

    "serialize to JSON" when {
      "the Eori value is valid" in {
        Json.toJson(validEori) shouldBe Json.obj("value" -> "GB123456789000")
      }
    }

    "deserialize from JSON" when {
      "the JSON contains a valid Eori value" in {
        val json = Json.obj("value" -> "GB123456789000")
        json.validate[Eori] shouldBe JsSuccess(validEori)
      }
      "has an extra field" in {
        val json = Json.obj("value" -> "GB123456789000", "extra" -> true)
        json.validate[Eori] shouldBe JsSuccess(validEori)
      }
    }

    "fail deserialization" when {
      "the value field is missing" in {
        val json = Json.obj()
        json.validate[Eori] shouldBe a[JsError]
      }

      "the value field is invalid" in {
        val json = Json.obj("value" -> 12345) // Invalid type
        json.validate[Eori] shouldBe a[JsError]
      }
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[Eori] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[Eori] shouldBe a[JsError]
      }
    }

    "handle edge cases" when {
      "value contains special characters" in {
        val eori = Eori("GB!@#$%^&*()")
        val json = Json.toJson(eori)

        json.validate[Eori] shouldBe JsSuccess(eori)
      }

      "value is an empty string" in {
        val eori = Eori("")
        val json = Json.toJson(eori)

        json.validate[Eori] shouldBe JsSuccess(eori)
      }

      "value is an unusually long string" in {
        val longValue = "G" * 1000
        val eori      = Eori(longValue)
        val json      = Json.toJson(eori)

        json.validate[Eori] shouldBe JsSuccess(eori)
      }
    }

    "correctly override toString" in {
      validEori.toString shouldBe "GB123456789000"
    }

    "support round-trip serialization/deserialization" in {
      val json = Json.toJson(validEori)
      json.validate[Eori] shouldBe JsSuccess(validEori)
    }
  }
}

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

import java.time.LocalDate

class JourneyDetailsEntrySpec extends AnyWordSpec with Matchers {

  "JourneyDetailsEntry" should {

    "serialize to JSON" when {
      "all fields are valid" in {
        val entry = JourneyDetailsEntry("LHR", LocalDate.parse("2024-12-10"))

        Json.toJson(entry) shouldBe Json.obj(
          "portCode"     -> "LHR",
          "dateOfTravel" -> "2024-12-10"
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "portCode"     -> "LHR",
          "dateOfTravel" -> "2024-12-10"
        )

        json.validate[JourneyDetailsEntry] shouldBe JsSuccess(
          JourneyDetailsEntry("LHR", LocalDate.parse("2024-12-10"))
        )
      }

      "fields are missing" in {
        val json = Json.obj(
          "portCode" -> "LHR"
          // Missing "dateOfTravel"
        )

        json.validate[JourneyDetailsEntry] shouldBe a[JsError]
      }

      "fields are of invalid types" in {
        val json = Json.obj(
          "portCode"     -> false,
          "dateOfTravel" -> true
        )

        json.validate[JourneyDetailsEntry] shouldBe a[JsError]
      }
    }

    "fail deserialization" when {
      "dateOfTravel is in an invalid format" in {
        val json = Json.obj(
          "portCode"     -> "LHR",
          "dateOfTravel" -> "invalid-date"
        )

        json.validate[JourneyDetailsEntry] shouldBe a[JsError]
      }
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[JourneyDetailsEntry] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[JourneyDetailsEntry] shouldBe a[JsError]
      }
    }

    "handle round-trip serialization/deserialization" in {
      val entry = JourneyDetailsEntry("LHR", LocalDate.parse("2024-12-10"))

      val json = Json.toJson(entry)
      json.validate[JourneyDetailsEntry] shouldBe JsSuccess(entry)
    }

    "ignore extra fields during deserialization" in {
      val json = Json.obj(
        "portCode"     -> "LHR",
        "dateOfTravel" -> "2024-12-10",
        "extraField"   -> "unexpected"
      )

      json.validate[JourneyDetailsEntry] shouldBe JsSuccess(
        JourneyDetailsEntry("LHR", LocalDate.parse("2024-12-10"))
      )
    }
  }
}

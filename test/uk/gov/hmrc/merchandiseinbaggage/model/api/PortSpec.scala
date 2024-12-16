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

class PortSpec extends AnyWordSpec with Matchers {
  "Port" should {

    val validPort = Port(
      code = "LHR",
      displayName = "London Heathrow",
      isGB = true,
      portSynonyms = List("Heathrow", "LHR", "London Airport")
    )

    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(validPort) shouldBe Json.obj(
          "code"         -> "LHR",
          "displayName"  -> "London Heathrow",
          "isGB"         -> true,
          "portSynonyms" -> Json.arr("Heathrow", "LHR", "London Airport")
        )
      }

      "portSynonyms is empty" in {
        val port = validPort.copy(portSynonyms = List.empty)
        Json.toJson(port) shouldBe Json.obj(
          "code"         -> "LHR",
          "displayName"  -> "London Heathrow",
          "isGB"         -> true,
          "portSynonyms" -> Json.arr()
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "code"         -> "LHR",
          "displayName"  -> "London Heathrow",
          "isGB"         -> true,
          "portSynonyms" -> Json.arr("Heathrow", "LHR", "London Airport")
        )

        json.validate[Port] shouldBe JsSuccess(validPort)
      }

      "portSynonyms is empty" in {
        val json = Json.obj(
          "code"         -> "LHR",
          "displayName"  -> "London Heathrow",
          "isGB"         -> true,
          "portSynonyms" -> Json.arr()
        )

        json.validate[Port] shouldBe JsSuccess(
          validPort.copy(portSynonyms = List.empty)
        )
      }
    }

    "fail deserialization" when {
      "required fields are missing" in {
        val json = Json.obj(
          "code"        -> "LHR",
          "displayName" -> "London Heathrow",
          "isGB"        -> true
          // Missing "portSynonyms"
        )

        json.validate[Port] shouldBe a[JsError]
      }

      "field types are invalid" in {
        val json = Json.obj(
          "code"         -> "LHR",
          "displayName"  -> "London Heathrow",
          "isGB"         -> "true", // Invalid type (should be boolean)
          "portSynonyms" -> Json.arr("Heathrow", "LHR", "London Airport")
        )

        json.validate[Port] shouldBe a[JsError]
      }

      "portSynonyms is null" in {
        val json = Json.obj(
          "code"         -> "LHR",
          "displayName"  -> "London Heathrow",
          "isGB"         -> true,
          "portSynonyms" -> null
        )

        json.validate[Port] shouldBe a[JsError]
      }
    }

    "handle edge cases" when {
      "fields contain special characters" in {
        val port = validPort.copy(
          code = "LHR@123",
          displayName = "London Heathrow!#$%",
          portSynonyms = List("Synonym@1", "Synonym#2", "Airport#$%")
        )

        val json = Json.toJson(port)
        json.validate[Port] shouldBe JsSuccess(port)
      }

      "portSynonyms contains a large list" in {
        val largeList = List.fill(1000)("Synonym")
        val port      = validPort.copy(portSynonyms = largeList)

        val json = Json.toJson(port)
        json.validate[Port] shouldBe JsSuccess(port)
      }
    }

    "support round-trip serialization/deserialization" in {
      val json = Json.toJson(validPort)
      json.validate[Port] shouldBe JsSuccess(validPort)
    }
  }

}

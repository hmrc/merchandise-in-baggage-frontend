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
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.{Address, AddressLookupCountry}

class CustomsAgentSpec extends AnyWordSpec with Matchers {

  "CustomsAgent" should {

    val validCountry = AddressLookupCountry("GB", Some("United Kingdom"))
    val validAddress = Address(Seq("Line 1", "Line 2"), Some("AB1 2CD"), validCountry)
    val validAgent   = CustomsAgent("John Doe", validAddress)

    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(validAgent) shouldBe Json.obj(
          "name"    -> "John Doe",
          "address" -> Json.obj(
            "lines"    -> Json.arr("Line 1", "Line 2"),
            "postcode" -> "AB1 2CD",
            "country"  -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          )
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "name"    -> "John Doe",
          "address" -> Json.obj(
            "lines"    -> Json.arr("Line 1", "Line 2"),
            "postcode" -> "AB1 2CD",
            "country"  -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          )
        )

        json.validate[CustomsAgent] shouldBe JsSuccess(validAgent)
      }

      "optional fields are missing in Address" in {
        val json = Json.obj(
          "name"    -> "John Doe",
          "address" -> Json.obj(
            "lines"   -> Json.arr("Line 1", "Line 2"),
            "country" -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          )
        )

        json.validate[CustomsAgent] shouldBe JsSuccess(
          CustomsAgent("John Doe", Address(Seq("Line 1", "Line 2"), None, validCountry))
        )
      }

      "name field is empty" in {
        val json = Json.obj(
          "name"    -> "",
          "address" -> Json.obj(
            "lines"    -> Json.arr("Line 1", "Line 2"),
            "postcode" -> "AB1 2CD",
            "country"  -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          )
        )

        json.validate[CustomsAgent] shouldBe JsSuccess(
          CustomsAgent(
            "",
            Address(Seq("Line 1", "Line 2"), Some("AB1 2CD"), AddressLookupCountry("GB", Some("United Kingdom")))
          )
        )
      }
    }

    "fail deserialization" when {
      "required fields are missing" in {
        val json = Json.obj(
          "address" -> Json.obj(
            "lines"    -> Json.arr("Line 1", "Line 2"),
            "postcode" -> "AB1 2CD",
            "country"  -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          )
        )

        json.validate[CustomsAgent] shouldBe a[JsError]
      }

      "field types are invalid" in {
        val json = Json.obj(
          "name"    -> "John Doe",
          "address" -> Json.obj(
            "lines"    -> "Line 1, Line 2",
            "postcode" -> "AB1 2CD",
            "country"  -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          )
        )

        json.validate[CustomsAgent] shouldBe a[JsError]
      }

      "country code is missing in Address" in {
        val json = Json.obj(
          "name"    -> "John Doe",
          "address" -> Json.obj(
            "lines"    -> Json.arr("Line 1", "Line 2"),
            "postcode" -> "AB1 2CD",
            "country"  -> Json.obj(
              "name" -> "United Kingdom"
            )
          )
        )

        json.validate[CustomsAgent] shouldBe a[JsError]
      }
    }

    "handle round-trip serialization/deserialization" in {
      val json = Json.toJson(validAgent)
      json.validate[CustomsAgent] shouldBe JsSuccess(validAgent)
    }

    "ignore extra fields in JSON" in {
      val json = Json.obj(
        "name"       -> "John Doe",
        "address"    -> Json.obj(
          "lines"    -> Json.arr("Line 1", "Line 2"),
          "postcode" -> "AB1 2CD",
          "country"  -> Json.obj(
            "code" -> "GB",
            "name" -> "United Kingdom"
          )
        ),
        "extraField" -> "unexpected"
      )

      json.validate[CustomsAgent] shouldBe JsSuccess(validAgent)
    }
  }

}

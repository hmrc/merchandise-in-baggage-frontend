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

class CountrySpec extends AnyWordSpec with Matchers {

  "Country" should {

    val validCountry = Country(
      code = "GBR",
      countryName = "United Kingdom",
      alphaTwoCode = "GB",
      isEu = false,
      countrySynonyms = List("Britain", "England")
    )

    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(validCountry) shouldBe Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> false,
          "countrySynonyms" -> Json.arr("Britain", "England")
        )
      }
      "countrySynonyms is empty and ensure no nulls" in {
        val countryWithEmptySynonyms = validCountry.copy(countrySynonyms = List.empty)

        Json.toJson(countryWithEmptySynonyms) shouldBe Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> false,
          "countrySynonyms" -> Json.arr()
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> false,
          "countrySynonyms" -> Json.arr("Britain", "England")
        )

        json.validate[Country] shouldBe JsSuccess(validCountry)
      }

      "countrySynonyms is empty" in {
        val json = Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> false,
          "countrySynonyms" -> Json.arr()
        )

        json.validate[Country] shouldBe JsSuccess(
          validCountry.copy(countrySynonyms = List.empty)
        )
      }

      "countrySynonyms is missing" in {
        val json = Json.obj(
          "code"         -> "GBR",
          "countryName"  -> "United Kingdom",
          "alphaTwoCode" -> "GB",
          "isEu"         -> false
        )

        json.validate[Country] shouldBe a[JsError]
      }
      "ignore extra fields in JSON" in {
        val json = Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> false,
          "countrySynonyms" -> Json.arr("Britain", "England"),
          "extraField"      -> "unexpected"
        )

        json.validate[Country] shouldBe JsSuccess(
          Country("GBR", "United Kingdom", "GB", isEu = false, List("Britain", "England"))
        )
      }
      "countrySynonyms array is empty" in {
        val json = Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> false,
          "countrySynonyms" -> Json.arr()
        )

        json.validate[Country] shouldBe JsSuccess(
          Country("GBR", "United Kingdom", "GB", isEu = false, List.empty)
        )
      }
      "countryName is an empty string" in {
        val json = Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> false,
          "countrySynonyms" -> Json.arr("Britain", "England")
        )

        json.validate[Country] shouldBe JsSuccess(
          validCountry.copy(countryName = "")
        )
      }
      "alphaTwoCode has special characters" in {
        val json = Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "G@",
          "isEu"            -> false,
          "countrySynonyms" -> Json.arr("Britain", "England")
        )

        json.validate[Country] shouldBe JsSuccess(
          validCountry.copy(alphaTwoCode = "G@")
        )
      }
      "special characters in countrySynonyms" in {
        val json = Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> false,
          "countrySynonyms" -> Json.arr("Brit@in", "Eng1and", "#Country!")
        )

        json.validate[Country] shouldBe JsSuccess(
          validCountry.copy(countrySynonyms = List("Brit@in", "Eng1and", "#Country!"))
        )
      }
    }

    "fail deserialization" when {
      "required fields are missing" in {
        val json = Json.obj(
          "code"        -> "GBR",
          "countryName" -> "United Kingdom"
          // Missing "alphaTwoCode", "isEu", and "countrySynonyms"
        )

        json.validate[Country] shouldBe a[JsError]
      }

      "field types are invalid" in {
        val json = Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> 123,
          "isEu"            -> "false",
          "countrySynonyms" -> Json.arr("Britain", "England")
        )

        json.validate[Country] shouldBe a[JsError]
      }
      "countrySynonyms is null" in {
        val jsonNull = Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> false,
          "countrySynonyms" -> null
        )
        jsonNull.validate[Country] shouldBe a[JsError]
      }
      "countrySynonyms is a string instead of an array" in {
        val json = Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> false,
          "countrySynonyms" -> "notAnArray"
        )

        json.validate[Country] shouldBe a[JsError]
      }

      "isEu is null" in {
        val json = Json.obj(
          "code"            -> "GBR",
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> null,
          "countrySynonyms" -> Json.arr("Britain", "England")
        )

        json.validate[Country] shouldBe a[JsError]
      }
      "code is empty or missing" in {
        val jsonMissingCode = Json.obj(
          "countryName"     -> "United Kingdom",
          "alphaTwoCode"    -> "GB",
          "isEu"            -> false,
          "countrySynonyms" -> Json.arr("Britain", "England")
        )

        jsonMissingCode.validate[Country] shouldBe a[JsError]
      }
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[Country] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[Country] shouldBe a[JsError]
      }

    }

    "support round-trip serialization/deserialization" in {
      val json = Json.toJson(validCountry)
      json.validate[Country] shouldBe JsSuccess(validCountry)
    }
    "support round-trip serialization/deserialization with edge cases" in {
      val edgeCaseCountry = validCountry.copy(
        alphaTwoCode = "X!",
        countryName = "Very Special #Name",
        countrySynonyms = List.empty
      )

      val json = Json.toJson(edgeCaseCountry)
      json.validate[Country] shouldBe JsSuccess(edgeCaseCountry)
    }
  }

}

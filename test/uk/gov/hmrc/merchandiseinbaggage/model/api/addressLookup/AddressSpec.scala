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

package uk.gov.hmrc.merchandiseinbaggage.model.api.addressLookup

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsArray, JsError, JsString, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.{Address, AddressLookupCountry}

class AddressSpec extends AnyWordSpecLike with Matchers {

  "AddressSpec" should {
    "serialize to JSON" when {
      "all fields are defined" in {
        val address = Address(Seq("test"), Some("NW10 6AD"), AddressLookupCountry("code", Some("name")))

        Json.toJson(address) shouldBe Json.obj(
          "lines"    -> JsArray(Seq(JsString("test"))),
          "postcode" -> "NW10 6AD",
          "country"  -> Json.obj("code" -> "code", "name" -> "name")
        )
      }

      "name is not defined" in {
        val address = Address(Seq("test"), Some("NW10 6AD"), AddressLookupCountry("code", None))

        Json.toJson(address) shouldBe Json.obj(
          "lines"    -> JsArray(Seq(JsString("test"))),
          "postcode" -> "NW10 6AD",
          "country"  -> Json.obj("code" -> "code")
        )
      }

      "postcode is not defined" in {
        val address = Address(Seq("test"), None, AddressLookupCountry("code", Some("name")))

        Json.toJson(address) shouldBe Json.obj(
          "lines"   -> JsArray(Seq(JsString("test"))),
          "country" -> Json.obj("code" -> "code", "name" -> "name")
        )
      }
    }
    "deserialize to JSON" when {
      "all fields are defined" in {
        val address = Address(Seq("test"), Some("NW10 6AD"), AddressLookupCountry("code", Some("name")))

        Json
          .obj(
            "lines"    -> JsArray(Seq(JsString("test"))),
            "postcode" -> "NW10 6AD",
            "country"  -> Json.obj("code" -> "code", "name" -> "name")
          )
          .validate[Address] shouldBe JsSuccess(address)
      }

      "name is not defined" in {
        val address = Address(Seq("test"), Some("NW10 6AD"), AddressLookupCountry("code", None))

        Json
          .obj(
            "lines"    -> JsArray(Seq(JsString("test"))),
            "postcode" -> "NW10 6AD",
            "country"  -> Json.obj("code" -> "code")
          )
          .validate[Address] shouldBe JsSuccess(address)
      }
      "postcode is not defined" in {
        val address = Address(Seq("test"), None, AddressLookupCountry("code", Some("name")))

        Json
          .obj(
            "lines"   -> JsArray(Seq(JsString("test"))),
            "country" -> Json.obj("code" -> "code", "name" -> "name")
          )
          .validate[Address] shouldBe JsSuccess(address)
      }

      "invalid Json address" in {
        Json
          .obj(
            "lines1"   -> JsArray(Seq(JsString("test"))),
            "country1" -> Json.obj("code" -> "code", "name" -> "name")
          )
          .validate[Address] shouldBe a[JsError]
      }

      "invalid Json address lookup" in {

        Json
          .obj(
            "lines"   -> JsArray(Seq(JsString("test"))),
            "country" -> Json.obj("code1" -> "code", "name1" -> "name")
          )
          .validate[Address] shouldBe a[JsError]
      }
      "an extra field is defined" in {
        val address = Address(Seq("test"), Some("NW10 6AD"), AddressLookupCountry("code", Some("name")))

        Json
          .obj(
            "lines"    -> JsArray(Seq(JsString("test"))),
            "postcode" -> "NW10 6AD",
            "country"  -> Json.obj("code" -> "code", "name" -> "name"),
            "extra"    -> ""
          )
          .validate[Address] shouldBe JsSuccess(address)
      }
      "an extra field is defined in AddressLookupCountry" in {
        val address = Address(Seq("test"), Some("NW10 6AD"), AddressLookupCountry("code", Some("name")))

        Json
          .obj(
            "lines"    -> JsArray(Seq(JsString("test"))),
            "postcode" -> "NW10 6AD",
            "country"  -> Json.obj("code" -> "code", "name" -> "name", "extra" -> true)
          )
          .validate[Address] shouldBe JsSuccess(address)
      }
      "fail to deserialize invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )

        json.validate[Address] shouldBe a[JsError]
      }
      "fail to deserialize an empty JSON object" in {
        val json = Json.obj()

        json.validate[Address] shouldBe a[JsError]
      }
    }
  }
}

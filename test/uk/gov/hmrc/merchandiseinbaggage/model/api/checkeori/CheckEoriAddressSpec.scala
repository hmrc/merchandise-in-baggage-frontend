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

package uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData

class CheckEoriAddressSpecextends extends AnyWordSpec with Matchers with CoreTestData {

  "CheckEoriAddress" should {
    "Serialize to json" when {
      "all the fields are present" in {
        Json.toJson(aCheckEoriAddress) shouldBe Json.obj(
          "streetAndNumber" -> "999 High Street",
          "cityName"        -> "CityName",
          "postcode"        -> "SS99 1AA"
        )
      }
      "address is not present" in {
        val checkEoriAddress: CheckEoriAddress = new CheckEoriAddress("", "", "")
        val json                               = Json.obj(
          "streetAndNumber" -> "",
          "cityName"        -> "",
          "postcode"        -> ""
        )
        Json.toJson(checkEoriAddress) shouldBe json
      }
      "streetAndNumber is not present" in {
        val checkEoriAddress: CheckEoriAddress = new CheckEoriAddress("", "a", "b")
        val json                               = Json.obj(
          "streetAndNumber" -> "",
          "cityName"        -> "a",
          "postcode"        -> "b"
        )
        Json.toJson(checkEoriAddress) shouldBe json
      }
      "cityName is not present" in {
        val checkEoriAddress: CheckEoriAddress = new CheckEoriAddress("a", "", "b")
        val json                               = Json.obj(
          "streetAndNumber" -> "a",
          "cityName"        -> "",
          "postcode"        -> "b"
        )
        Json.toJson(checkEoriAddress) shouldBe json
      }
      "postcode is not present" in {
        val checkEoriAddress: CheckEoriAddress = new CheckEoriAddress("b", "a", "")
        val json                               = Json.obj(
          "streetAndNumber" -> "b",
          "cityName"        -> "a",
          "postcode"        -> ""
        )
        Json.toJson(checkEoriAddress) shouldBe json
      }
    }
    "Deserialize json" when {
      "all the fields are present" in {
        val json = Json.obj(
          "streetAndNumber" -> "999 High Street",
          "cityName"        -> "CityName",
          "postcode"        -> "SS99 1AA"
        )
        json.validate[CheckEoriAddress] shouldBe JsSuccess(aCheckEoriAddress)
      }
      "address is not present" in {
        val checkEoriAddress: CheckEoriAddress = new CheckEoriAddress("", "", "")
        val json                               = Json.obj(
          "streetAndNumber" -> "",
          "cityName"        -> "",
          "postcode"        -> ""
        )
        json.validate[CheckEoriAddress] shouldBe JsSuccess(checkEoriAddress)
      }
      "streetAndNumber is not present" in {
        val checkEoriAddress: CheckEoriAddress = new CheckEoriAddress("", "a", "b")
        val json                               = Json.obj(
          "streetAndNumber" -> "",
          "cityName"        -> "a",
          "postcode"        -> "b"
        )
        json.validate[CheckEoriAddress] shouldBe JsSuccess(checkEoriAddress)
      }
      "cityName is not present" in {
        val checkEoriAddress: CheckEoriAddress = new CheckEoriAddress("a", "", "b")
        val json                               = Json.obj(
          "streetAndNumber" -> "a",
          "cityName"        -> "",
          "postcode"        -> "b"
        )
        json.validate[CheckEoriAddress] shouldBe JsSuccess(checkEoriAddress)
      }
      "postcode is not present" in {
        val checkEoriAddress: CheckEoriAddress = new CheckEoriAddress("b", "a", "")
        val json                               = Json.obj(
          "streetAndNumber" -> "b",
          "cityName"        -> "a",
          "postcode"        -> ""
        )
        json.validate[CheckEoriAddress] shouldBe JsSuccess(checkEoriAddress)
      }
      "there is an extra argument" in {
        val checkEoriAddress: CheckEoriAddress = new CheckEoriAddress("b", "a", "c")
        val json                               = Json.obj(
          "streetAndNumber" -> "b",
          "cityName"        -> "a",
          "postcode"        -> "c",
          "extra"           -> 1
        )
        json.validate[CheckEoriAddress] shouldBe JsSuccess(checkEoriAddress)
      }
      "fail to deserialize invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )

        json.validate[CheckEoriAddress] shouldBe a[JsError]
      }
      "fail to deserialize an empty JSON object" in {
        val json = Json.obj()

        json.validate[CheckEoriAddress] shouldBe a[JsError]
      }
    }
  }
}

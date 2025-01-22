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

package uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData

class CheckResponseSpec extends AnyWordSpec with Matchers with CoreTestData {

  "CheckResponse" should {
    "Serialize to json" when {
      "all the fields are present" in {
        Json.toJson(aCheckResponse) shouldBe Json.obj(
          "eori"           -> "GB025115110987654",
          "valid"          -> true,
          "companyDetails" -> Json.obj(
            "traderName" -> "Firstname LastName",
            "address"    -> Json.obj(
              "streetAndNumber" -> "999 High Street",
              "cityName"        -> "CityName",
              "postcode"        -> "SS99 1AA"
            )
          )
        )
      }
      "companyDetails is not present" in {
        Json.toJson(CheckResponse(aEoriNumber, valid = true, None)) shouldBe Json.obj(
          "eori"  -> "GB025115110987654",
          "valid" -> true
        )
      }
      "eori number is not present" in {
        Json.toJson(CheckResponse("", valid = false, Some(aCompanyDetails))) shouldBe Json.obj(
          "eori"           -> "",
          "valid"          -> false,
          "companyDetails" -> Json.obj(
            "traderName" -> "Firstname LastName",
            "address"    -> Json.obj(
              "streetAndNumber" -> "999 High Street",
              "cityName"        -> "CityName",
              "postcode"        -> "SS99 1AA"
            )
          )
        )
      }
    }
    "Deserialize to json" when {
      "all the fields are present" in {
        val json = Json.obj(
          "eori"           -> "GB025115110987654",
          "valid"          -> true,
          "companyDetails" -> Json.obj(
            "traderName" -> "Firstname LastName",
            "address"    -> Json.obj(
              "streetAndNumber" -> "999 High Street",
              "cityName"        -> "CityName",
              "postcode"        -> "SS99 1AA"
            )
          )
        )
        json.validate[CheckResponse] shouldBe JsSuccess(aCheckResponse)
      }
      "an extra field is present" in {
        val json = Json.obj(
          "eori"           -> "GB025115110987654",
          "valid"          -> true,
          "companyDetails" -> Json.obj(
            "traderName" -> "Firstname LastName",
            "address"    -> Json.obj(
              "streetAndNumber" -> "999 High Street",
              "cityName"        -> "CityName",
              "postcode"        -> "SS99 1AA"
            )
          ),
          "extra"          -> true
        )
        json.validate[CheckResponse] shouldBe JsSuccess(aCheckResponse)
      }
      "companyDetails is not present" in {
        val json = Json.obj(
          "eori"  -> "GB025115110987654",
          "valid" -> true
        )
        json.validate[CheckResponse] shouldBe JsSuccess(CheckResponse(aEoriNumber, valid = true, None))
      }
      "eori number is not present" in {
        val json = Json.obj(
          "valid"          -> false,
          "companyDetails" -> Json.obj(
            "traderName" -> "Firstname LastName",
            "address"    -> Json.obj(
              "streetAndNumber" -> "999 High Street",
              "cityName"        -> "CityName",
              "postcode"        -> "SS99 1AA"
            )
          )
        )
        json.validate[CheckResponse] shouldBe a[JsError]
      }
    }
    "valid is not present" in {
      val json = Json.obj(
        "eori"           -> "678742903",
        "companyDetails" -> Json.obj(
          "traderName" -> "Firstname LastName",
          "address"    -> Json.obj(
            "streetAndNumber" -> "999 High Street",
            "cityName"        -> "CityName",
            "postcode"        -> "SS99 1AA"
          )
        )
      )
      json.validate[CheckResponse] shouldBe a[JsError]
    }
    "fail to deserialize invalid JSON structure" in {
      val json = Json.arr(
        Json.obj("key" -> "value")
      )

      json.validate[CheckResponse] shouldBe a[JsError]
    }
    "fail to deserialize an empty JSON object" in {
      val json = Json.obj()

      json.validate[CheckResponse] shouldBe a[JsError]
    }
  }
}

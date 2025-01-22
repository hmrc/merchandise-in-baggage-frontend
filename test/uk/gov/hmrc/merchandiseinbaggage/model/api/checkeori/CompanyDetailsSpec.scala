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

class CompanyDetailsSpec extends AnyWordSpec with Matchers with CoreTestData {

  "CompanyDetails" should {
    "Serialize to json" when {
      "all the fields are present" in {
        Json.toJson(aCompanyDetails) shouldBe Json.obj(
          "traderName" -> "Firstname LastName",
          "address"    -> Json.obj(
            "streetAndNumber" -> "999 High Street",
            "cityName"        -> "CityName",
            "postcode"        -> "SS99 1AA"
          )
        )
      }
    }
    "Deserialize to json" when {
      "all the fields are present" in {
        val json = Json.obj(
          "traderName" -> "Firstname LastName",
          "address"    -> Json.obj(
            "streetAndNumber" -> "999 High Street",
            "cityName"        -> "CityName",
            "postcode"        -> "SS99 1AA"
          )
        )
        json.validate[CompanyDetails] shouldBe JsSuccess(aCompanyDetails)
      }
      "an extra field is present" in {
        val json = Json.obj(
          "traderName" -> "Firstname LastName",
          "address"    -> Json.obj(
            "streetAndNumber" -> "999 High Street",
            "cityName"        -> "CityName",
            "postcode"        -> "SS99 1AA",
            "extra"           -> true
          )
        )
        json.validate[CompanyDetails] shouldBe JsSuccess(aCompanyDetails)
      }
      "traderName is not present" in {
        val json = Json.obj(
          "address" -> Json.obj(
            "streetAndNumber" -> "999 High Street",
            "cityName"        -> "CityName",
            "postcode"        -> "SS99 1AA"
          )
        )
        json.validate[CompanyDetails] shouldBe a[JsError]
      }
      "fail to deserialize invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )

        json.validate[CompanyDetails] shouldBe a[JsError]
      }
      "fail to deserialize an empty JSON object" in {
        val json = Json.obj()

        json.validate[CompanyDetails] shouldBe a[JsError]
      }
    }
  }
}

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

package uk.gov.hmrc.merchandiseinbaggage.model.api.payapi

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.core.URL

class PayApiResponseSpec extends AnyWordSpec with Matchers {
  "PayApiResponse" should {
    "Serialize to json" when {
      "all the fields are present" in {
        Json.toJson(PayApiResponse(JourneyId("abc"), URL("xyz"))) shouldBe Json.obj(
          "journeyId" -> "abc",
          "nextUrl"   -> "xyz"
        )
      }
      "JourneyId is not present" in {
        Json.toJson(PayApiResponse(JourneyId(""), URL("xyz"))) shouldBe Json.obj("journeyId" -> "", "nextUrl" -> "xyz")
      }
      "URL is not present" in {
        Json.toJson(PayApiResponse(JourneyId("abc"), URL(""))) shouldBe Json.obj("journeyId" -> "abc", "nextUrl" -> "")
      }
    }
    "Deserialize json" when {
      "all the fields are present" in {
        val json = Json.obj("journeyId" -> "abc", "nextUrl" -> "xyz")
        json.validate[PayApiResponse] shouldBe JsSuccess(PayApiResponse(JourneyId("abc"), URL("xyz")))
      }
      "an extra field is present" in {
        val json = Json.obj("journeyId" -> "abc", "nextUrl" -> "xyz", "extra" -> true)
        json.validate[PayApiResponse] shouldBe JsSuccess(PayApiResponse(JourneyId("abc"), URL("xyz")))
      }
      "URL is not present" in {
        val json = Json.obj("journeyId" -> "abc")
        json.validate[PayApiResponse] shouldBe a[JsError]
      }
      "journeyId is not present" in {
        val json = Json.obj("nextUrl" -> "xyz")
        json.validate[PayApiResponse] shouldBe a[JsError]
      }
      "fail to deserialize invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[PayApiResponse] shouldBe a[JsError]
      }
      "fail to deserialize an empty JSON object" in {
        val json = Json.obj()
        json.validate[PayApiResponse] shouldBe a[JsError]
      }
    }
  }
}

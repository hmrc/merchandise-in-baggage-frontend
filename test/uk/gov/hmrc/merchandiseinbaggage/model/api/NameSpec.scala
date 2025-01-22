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

class NameSpec extends AnyWordSpec with Matchers {
  "Name" should {
    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(Name("first", "last")) shouldBe Json.obj("firstName" -> "first", "lastName" -> "last")
      }
    }
    "deserialize to JSON" when {
      "all fields are valid" in {
        val json = Json.obj("firstName" -> "first", "lastName" -> "last")
        json.validate[Name] shouldBe JsSuccess(Name("first", "last"))
      }
      "there is an extra field" in {
        val json = Json.obj("firstName" -> "first", "lastName" -> "last", "extra" -> 1)
        json.validate[Name] shouldBe JsSuccess(Name("first", "last"))
      }
      "first name is not defined" in {
        val json = Json.obj("firstName" -> "", "lastName" -> "last")
        json.validate[Name] shouldBe JsSuccess(Name("", "last"))
      }
      "last name is not defined" in {
        val json = Json.obj("firstName" -> "first", "lastName" -> "")
        json.validate[Name] shouldBe JsSuccess(Name("first", ""))
      }
    }
    "fail to deserialize to json" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[Name] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[Name] shouldBe a[JsError]
      }
    }
  }
}

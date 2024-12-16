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

class EmailSpec extends AnyWordSpec with Matchers {
  val validEmail = Email("example@test.com")

  "Email" should {

    "serialize to JSON" when {
      "the Email value is valid" in {
        Json.toJson(validEmail) shouldBe Json.obj("email" -> "example@test.com")
      }
    }

    "deserialize from JSON" when {
      "the JSON contains a valid Email value" in {
        val json = Json.obj("email" -> "example@test.com")
        json.validate[Email] shouldBe JsSuccess(validEmail)
      }
    }

    "fail deserialization" when {
      "the email field is missing" in {
        val json = Json.obj()
        json.validate[Email] shouldBe a[JsError]
      }

      "the email field is invalid" in {
        val json = Json.obj("email" -> 12345) // Invalid type
        json.validate[Email] shouldBe a[JsError]
      }
    }

    "handle edge cases" when {
      "email contains special characters" in {
        val email = Email("example+123@test.com")
        val json  = Json.toJson(email)

        json.validate[Email] shouldBe JsSuccess(email)
      }

      "email is an empty string" in {
        val email = Email("")
        val json  = Json.toJson(email)

        json.validate[Email] shouldBe JsSuccess(email)
      }
    }

    "support round-trip serialization/deserialization" in {
      val json = Json.toJson(validEmail)
      json.validate[Email] shouldBe JsSuccess(validEmail)
    }
  }
}

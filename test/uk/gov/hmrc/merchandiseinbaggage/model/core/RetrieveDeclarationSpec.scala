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

package uk.gov.hmrc.merchandiseinbaggage.model.core

import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

class RetrieveDeclarationSpec extends BaseSpecWithApplication with CoreTestData {
  "RetrieveDeclaration" should {
    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(RetrieveDeclaration(mibReference, eori)) shouldBe Json.obj(
          "mibReference" -> "XAMB0000010000",
          "eori"         -> Json.obj("value" -> "GB123456780000")
        )
      }
    }
    "deserialize to JSON" when {
      "all fields are valid" in {
        val json = Json.obj("mibReference" -> "XAMB0000010000", "eori" -> Json.obj("value" -> "GB123456780000"))
        json.validate[RetrieveDeclaration] shouldBe JsSuccess(RetrieveDeclaration(mibReference, eori))
      }
    }
    "fail to deserialize" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[RetrieveDeclaration] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[RetrieveDeclaration] shouldBe a[JsError]
      }
    }
  }
}

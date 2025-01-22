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

import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.{JsError, Json}
import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

class PurchaseDetailsSpec extends BaseSpecWithApplication with CoreTestData {
  "PurchaseDetails" should {
    "serialise and de-serialise" in {
      parse(toJson(aPurchaseDetails).toString()).validate[PurchaseDetails].get mustBe aPurchaseDetails
    }
    "fail to deserialize" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[PurchaseDetails] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[PurchaseDetails] shouldBe a[JsError]
      }
    }
  }
}

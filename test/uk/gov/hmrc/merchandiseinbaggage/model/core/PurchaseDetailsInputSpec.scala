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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}

class PurchaseDetailsInputSpec extends AnyWordSpec with Matchers {
  "PurchaseDetailsInput" should {
    "serialize to JSON" when {
      "with all fields are defined" in {
        val purchaseDetails = PurchaseDetailsInput("12.3", "UCD")

        Json.toJson(purchaseDetails) shouldBe Json.obj(
          "price"    -> "12.3",
          "currency" -> "UCD"
        )
      }

      "with price not defined" in {
        val purchaseDetails = PurchaseDetailsInput("", "UCD")

        Json.toJson(purchaseDetails) shouldBe Json.obj(
          "price"    -> "",
          "currency" -> "UCD"
        )
      }

      "with currency not defined" in {
        val purchaseDetails = PurchaseDetailsInput("2", "")

        Json.toJson(purchaseDetails) shouldBe Json.obj("price" -> "2", "currency" -> "")
      }

      "with neither currency nor price defined" in {
        val purchaseDetails = PurchaseDetailsInput("", "")

        Json.toJson(purchaseDetails) shouldBe Json.obj("price" -> "", "currency" -> "")
      }
    }

    "deserialize to JSON" when {
      "with all fields are defined" in {
        val purchaseDetails = PurchaseDetailsInput("12.3", "UCD")
        val json            = Json.obj(
          "price"    -> "12.3",
          "currency" -> "UCD"
        )
        json.validate[PurchaseDetailsInput] shouldBe JsSuccess(purchaseDetails)
      }

      "with price not defined" in {
        val purchaseDetails = PurchaseDetailsInput("", "UCD")
        val json            = Json.obj(
          "price"    -> "",
          "currency" -> "UCD"
        )
        json.validate[PurchaseDetailsInput] shouldBe JsSuccess(purchaseDetails)
      }

      "with currency not defined" in {
        val purchaseDetails = PurchaseDetailsInput("2", "")
        val json            = Json.obj("price" -> "2", "currency" -> "")
        json.validate[PurchaseDetailsInput] shouldBe JsSuccess(purchaseDetails)
      }

      "with neither currency nor price defined" in {
        val purchaseDetails = PurchaseDetailsInput("", "")
        val json            = Json.obj("price" -> "", "currency" -> "")
        json.validate[PurchaseDetailsInput] shouldBe JsSuccess(purchaseDetails)
      }

      "with price not present" in {
        val json = Json.obj(
          "currency" -> "UCD"
        )
        json.validate[PurchaseDetailsInput] shouldBe a[JsError]
      }

      "with currency not present" in {
        val json = Json.obj("price" -> "2")
        json.validate[PurchaseDetailsInput] shouldBe a[JsError]
      }

      "with an extra argument" in {
        val purchaseDetails = PurchaseDetailsInput("2", "UCD")
        val json            = Json.obj("price" -> "2", "currency" -> "UCD", "extra" -> 1)
        json.validate[PurchaseDetailsInput] shouldBe JsSuccess(purchaseDetails)
      }

      "fail to deserialize invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )

        json.validate[PurchaseDetailsInput] shouldBe a[JsError]
      }
      "fail to deserialize an empty JSON object" in {
        val json = Json.obj()

        json.validate[PurchaseDetailsInput] shouldBe a[JsError]
      }
    }
  }
}

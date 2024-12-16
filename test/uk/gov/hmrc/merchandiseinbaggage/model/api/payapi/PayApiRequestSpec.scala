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

package uk.gov.hmrc.merchandiseinbaggage.model.api.payapi

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, MibReference, payapi}
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse

class PayApiRequestSpec extends AnyWordSpec with Matchers with CoreTestData {

  "CalculationResults" should {
    "Serialize to json" when {
      "all the fields are present" in {
        Json.toJson(payApiRequest) shouldBe Json.obj(
          "backUrl"           ->
            "http://localhost:8281/declare-commercial-goods/check-your-answers",
          "dutyAmountInPence" -> 3,
          "returnUrl"         -> "http://localhost:8281/declare-commercial-goods/declaration-confirmation",
          "mibReference"      -> "MIBI1234567890",
          "amountInPence"     -> 1,
          "vatAmountInPence"  -> 2
        )
      }
    }
    "Deserialize to json" when {
      "all the fields are present" in {
        val json = Json.obj(
          "backUrl"           ->
            "http://localhost:8281/declare-commercial-goods/check-your-answers",
          "dutyAmountInPence" -> 3,
          "returnUrl"         -> "http://localhost:8281/declare-commercial-goods/declaration-confirmation",
          "mibReference"      -> "MIBI1234567890",
          "amountInPence"     -> 1,
          "vatAmountInPence"  -> 2
        )
        json.validate[PayApiRequest] shouldBe JsSuccess(payApiRequest)
      }
      "when an extra field is present" in {
        val json = Json.obj(
          "backUrl"           ->
            "http://localhost:8281/declare-commercial-goods/check-your-answers",
          "dutyAmountInPence" -> 3,
          "returnUrl"         -> "http://localhost:8281/declare-commercial-goods/declaration-confirmation",
          "mibReference"      -> "MIBI1234567890",
          "amountInPence"     -> 1,
          "vatAmountInPence"  -> 2,
          "extra"             -> true
        )
        json.validate[PayApiRequest] shouldBe JsSuccess(payApiRequest)
      }
      "when backUrl is not present" in {
        val json = Json.obj(
          "dutyAmountInPence" -> 3,
          "returnUrl"         -> "http://localhost:8281/declare-commercial-goods/declaration-confirmation",
          "mibReference"      -> "MIBI1234567890",
          "amountInPence"     -> 1,
          "vatAmountInPence"  -> 2
        )
        json.validate[PayApiRequest] shouldBe a[JsError]
      }
      "when dutyAmountInPence is not present" in {
        val json = Json.obj(
          "backUrl"          ->
            "http://localhost:8281/declare-commercial-goods/check-your-answers",
          "returnUrl"        -> "http://localhost:8281/declare-commercial-goods/declaration-confirmation",
          "mibReference"     -> "MIBI1234567890",
          "amountInPence"    -> 1,
          "vatAmountInPence" -> 2
        )
        json.validate[PayApiRequest] shouldBe a[JsError]
      }
      "when mibReference is not present" in {
        val json = Json.obj(
          "backUrl"           ->
            "http://localhost:8281/declare-commercial-goods/check-your-answers",
          "dutyAmountInPence" -> 3,
          "returnUrl"         -> "http://localhost:8281/declare-commercial-goods/declaration-confirmation",
          "amountInPence"     -> 1,
          "vatAmountInPence"  -> 2
        )
        json.validate[PayApiRequest] shouldBe a[JsError]
      }
      "when returnurl is not present" in {
        val json = Json.obj(
          "backUrl"           ->
            "http://localhost:8281/declare-commercial-goods/check-your-answers",
          "dutyAmountInPence" -> 3,
          "mibReference"      -> "MIBI1234567890",
          "amountInPence"     -> 1,
          "vatAmountInPence"  -> 2
        )
        json.validate[PayApiRequest] shouldBe a[JsError]
      }
      "when amountInPence is not present" in {
        val json = Json.obj(
          "backUrl"           ->
            "http://localhost:8281/declare-commercial-goods/check-your-answers",
          "dutyAmountInPence" -> 3,
          "returnUrl"         -> "http://localhost:8281/declare-commercial-goods/declaration-confirmation",
          "mibReference"      -> "MIBI1234567890",
          "vatAmountInPence"  -> 2
        )
        json.validate[PayApiRequest] shouldBe a[JsError]
      }
      "when vatAmountInPence is not present" in {
        val json = Json.obj(
          "backUrl"           ->
            "http://localhost:8281/declare-commercial-goods/check-your-answers",
          "dutyAmountInPence" -> 3,
          "returnUrl"         -> "http://localhost:8281/declare-commercial-goods/declaration-confirmation",
          "mibReference"      -> "MIBI1234567890",
          "amountInPence"     -> 1
        )
        json.validate[PayApiRequest] shouldBe a[JsError]
      }
      "fail to deserialize invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )

        json.validate[PayApiRequest] shouldBe a[JsError]
      }
      "fail to deserialize an empty JSON object" in {
        val json = Json.obj()

        json.validate[PayApiRequest] shouldBe a[JsError]
      }
    }
  }
}

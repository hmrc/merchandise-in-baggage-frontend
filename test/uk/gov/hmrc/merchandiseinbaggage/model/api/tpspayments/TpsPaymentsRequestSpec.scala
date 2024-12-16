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

package uk.gov.hmrc.merchandiseinbaggage.model.api.tpspayments

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse

class TpsPaymentsRequestSpec extends AnyWordSpec with Matchers with CoreTestData {

  "TpsPaymentsRequest" should {
    val tpsPaymentsRequest: TpsPaymentsRequest = new TpsPaymentsRequest(
      "mibReference": String,
      "customerName": String,
      1: BigDecimal,
      None: Option[Int],
      1: BigDecimal,
      1: BigDecimal,
      "backUrl": String,
      "resetUrl": String,
      "finishUrl": String
    )
    val json                                   = Json.obj(
      "totalVatDue"  -> 1,
      "mibReference" -> "mibReference",
      "customerName" -> "customerName",
      "totalDutyDue" -> 1,
      "amount"       -> 1,
      "backUrl"      -> "backUrl",
      "resetUrl"     -> "resetUrl",
      "finishUrl"    -> "finishUrl"
    )
    "Serialize to json" when {
      "all the fields are present" in {
        Json.toJson(tpsPaymentsRequest) shouldBe json
      }
      "mibReference is missing" in {
        Json.toJson(
          new TpsPaymentsRequest(
            "",
            "customerName": String,
            1: BigDecimal,
            None: Option[Int],
            1: BigDecimal,
            1: BigDecimal,
            "backUrl": String,
            "resetUrl": String,
            "finishUrl": String
          )
        ) shouldBe Json.obj(
          "totalVatDue"  -> 1,
          "mibReference" -> "",
          "customerName" -> "customerName",
          "totalDutyDue" -> 1,
          "amount"       -> 1,
          "backUrl"      -> "backUrl",
          "resetUrl"     -> "resetUrl",
          "finishUrl"    -> "finishUrl"
        )
      }
      "customerName is missing" in {
        Json.toJson(
          new TpsPaymentsRequest(
            "mibReference",
            "",
            1: BigDecimal,
            None: Option[Int],
            1: BigDecimal,
            1: BigDecimal,
            "backUrl": String,
            "resetUrl": String,
            "finishUrl": String
          )
        ) shouldBe Json.obj(
          "totalVatDue"  -> 1,
          "mibReference" -> "mibReference",
          "customerName" -> "",
          "totalDutyDue" -> 1,
          "amount"       -> 1,
          "backUrl"      -> "backUrl",
          "resetUrl"     -> "resetUrl",
          "finishUrl"    -> "finishUrl"
        )
      }
      "all the properties are missing" in {
        Json.toJson(
          new TpsPaymentsRequest(
            "",
            "",
            0: BigDecimal,
            None: Option[Int],
            0: BigDecimal,
            0: BigDecimal,
            "": String,
            "": String,
            "": String
          )
        ) shouldBe Json.obj(
          "totalVatDue"  -> 0,
          "mibReference" -> "",
          "customerName" -> "",
          "totalDutyDue" -> 0,
          "amount"       -> 0,
          "backUrl"      -> "",
          "resetUrl"     -> "",
          "finishUrl"    -> ""
        )
      }
    }
    "Deserialize to json" when {
      "all the fields are present" in {
        json.validate[TpsPaymentsRequest] shouldBe JsSuccess(tpsPaymentsRequest)
      }
      "mibReference is missing" in {
        val json = Json.obj(
          "totalVatDue"  -> 1,
          "customerName" -> "customerName",
          "totalDutyDue" -> 1,
          "amount"       -> 1,
          "backUrl"      -> "backUrl",
          "resetUrl"     -> "resetUrl",
          "finishUrl"    -> "finishUrl"
        )
        json.validate[TpsPaymentsRequest] shouldBe a[JsError]
      }
      "There is an extra attribute" in {
        val json = Json.obj(
          "totalVatDue"  -> 1,
          "mibReference" -> "mibReference",
          "customerName" -> "customerName",
          "totalDutyDue" -> 1,
          "amount"       -> 1,
          "backUrl"      -> "backUrl",
          "resetUrl"     -> "resetUrl",
          "finishUrl"    -> "finishUrl",
          "exttra"       -> true
        )
        json.validate[TpsPaymentsRequest] shouldBe JsSuccess(tpsPaymentsRequest)
      }
    }
  }
}

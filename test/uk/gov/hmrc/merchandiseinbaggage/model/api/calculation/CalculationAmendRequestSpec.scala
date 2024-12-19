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

package uk.gov.hmrc.merchandiseinbaggage.model.api.calculation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, DeclarationGoods, DeclarationId, GoodsDestinations}

import java.time.LocalDateTime

class CalculationAmendRequestSpec extends AnyWordSpec with Matchers with CoreTestData {

  lazy val timestamp = LocalDateTime.parse("2024-12-09T16:38:37.598")

  lazy val allFieldsDefinedJson = Json.obj(
    "declarationId"         -> "decl1",
    "amend"                 -> Json.obj(
      "reference"                   -> 1,
      "source"                      -> "Digital",
      "emailsSent"                  -> false,
      "dateOfAmendment"             -> "2024-12-09T16:38:37.598",
      "goods"                       -> Json.obj(
        "goods" -> Json.arr(
          Json.obj(
            "category"        -> "test",
            "goodsVatRate"    -> "Five",
            "producedInEu"    -> "Yes",
            "purchaseDetails" -> Json.obj(
              "amount"   -> "100",
              "currency" -> Json.obj(
                "code"               -> "GBP",
                "displayName"        -> "title.euro_eur",
                "currencySynonyms"   -> Json.arr("Europe", "European"),
                "valueForConversion" -> "GBP"
              )
            )
          )
        )
      ),
      "lang"                        -> "en",
      "maybeTotalCalculationResult" -> Json.obj(
        "totalVatDue"        -> 100,
        "calculationResults" -> Json.obj(
          "calculationResults" -> Json.arr(
            Json.obj(
              "goods"     -> Json.obj(
                "category"        -> "test",
                "goodsVatRate"    -> "Five",
                "producedInEu"    -> "Yes",
                "purchaseDetails" -> Json.obj(
                  "amount"   -> "100",
                  "currency" -> Json.obj(
                    "code"               -> "GBP",
                    "displayName"        -> "title.euro_eur",
                    "currencySynonyms"   -> Json.arr("Europe", "European"),
                    "valueForConversion" -> "GBP"
                  )
                )
              ),
              "gbpAmount" -> 100,
              "duty"      -> 100,
              "vat"       -> 100
            )
          )
        ),
        "totalDutyDue"       -> 100,
        "totalGbpValue"      -> 100,
        "totalTaxDue"        -> 100
      )
    ),
    "maybeGoodsDestination" -> "GreatBritain"
  )

  "CalculationAmendRequest" should {

    "serialize to JSON" when {

      "optional fields are None" in {
        val request = CalculationAmendRequest(
          amend = None,
          maybeGoodsDestination = None,
          declarationId = DeclarationId("decl1")
        )

        Json.toJson(request) shouldBe Json.obj(
          "declarationId" -> "decl1"
        )
      }
    }

    "serialize and deserialize from JSON" when {
      "all fields are defined" in {
        val json    = allFieldsDefinedJson
        val request = json.validate[CalculationAmendRequest]
        json.validate[CalculationAmendRequest] shouldBe request
      }
    }

    "deserialize from JSON" when {
      "optional fields are missing" in {
        val json = Json.obj(
          "declarationId" -> "decl1"
        )

        json.validate[CalculationAmendRequest] shouldBe JsSuccess(
          CalculationAmendRequest(
            amend = None,
            maybeGoodsDestination = None,
            declarationId = DeclarationId("decl1")
          )
        )
      }

      "some fields are null" in {
        val json = Json.obj(
          "amend"                 -> null,
          "maybeGoodsDestination" -> null,
          "declarationId"         -> "decl1"
        )

        json.validate[CalculationAmendRequest] shouldBe JsSuccess(
          CalculationAmendRequest(
            amend = None,
            maybeGoodsDestination = None,
            declarationId = DeclarationId("decl1")
          )
        )
      }

      "required fields are missing" in {
        val json = Json.obj(
          "amend" -> Json.obj("id" -> "amend1")
        )

        json.validate[CalculationAmendRequest] shouldBe a[JsError]
      }

      "fields are of invalid types" in {
        val json = Json.obj(
          "amend"                 -> "invalidType",
          "maybeGoodsDestination" -> 12345,
          "declarationId"         -> Json.obj("value" -> true)
        )

        json.validate[CalculationAmendRequest] shouldBe a[JsError]
      }

      "extra fields are included" in {
        val json    = allFieldsDefinedJson + ("extraField" -> JsString("unexpected"))
        val request = json.validate[CalculationAmendRequest]
        json.validate[CalculationAmendRequest] shouldBe request
      }

      "fail to deserialize invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )

        json.validate[CalculationAmendRequest] shouldBe a[JsError]
      }
      "fail to deserialize an empty JSON object" in {
        val json = Json.obj()

        json.validate[CalculationAmendRequest] shouldBe a[JsError]
      }
      "fail to deserialize partial JSON with invalid types" in {
        val json = Json.obj(
          "declarationId"         -> "decl1",
          "amend"                 -> Json.obj("reference" -> "invalidType"),
          "maybeGoodsDestination" -> "GreatBritain"
        )

        json.validate[CalculationAmendRequest] shouldBe a[JsError]
      }
      "fail to deserialize with missing nested fields" in {
        val json = Json.obj(
          "declarationId"         -> "decl1",
          "amend"                 -> Json.obj(
            "source"     -> "Digital",
            "emailsSent" -> false
            // Missing nested fields like "reference" and "dateOfAmendment"
          ),
          "maybeGoodsDestination" -> "GreatBritain"
        )

        json.validate[CalculationAmendRequest] shouldBe a[JsError]
      }
      "null nested optional fields" in {
        val json = Json.obj(
          "declarationId"         -> "decl1",
          "amend"                 -> Json.obj(
            "reference"                   -> 1,
            "source"                      -> "Digital",
            "emailsSent"                  -> false,
            "dateOfAmendment"             -> "2024-12-09T16:38:37.598",
            "goods"                       -> Json.obj("goods" -> Json.arr()),
            "lang"                        -> "en",
            "maybeTotalCalculationResult" -> null,
            "paymentStatus"               -> null
          ),
          "maybeGoodsDestination" -> "GreatBritain"
        )

        json.validate[CalculationAmendRequest] shouldBe JsSuccess(
          CalculationAmendRequest(
            amend = Some(
              Amendment(
                reference = 1,
                dateOfAmendment = LocalDateTime.parse("2024-12-09T16:38:37.598"),
                goods = DeclarationGoods(List()),
                maybeTotalCalculationResult = None,
                paymentStatus = None,
                source = Some("Digital"),
                emailsSent = false,
                lang = "en"
              )
            ),
            maybeGoodsDestination = Some(GoodsDestinations.GreatBritain),
            declarationId = DeclarationId("decl1")
          )
        )
      }

    }

    "support round-trip serialization/deserialization" in {
      val request = CalculationAmendRequest(
        amend = Some(aAmendment.copy(dateOfAmendment = timestamp)),
        maybeGoodsDestination = Some(GoodsDestinations.GreatBritain),
        declarationId = DeclarationId("decl1")
      )

      val json = Json.toJson(request)
      json.validate[CalculationAmendRequest] shouldBe JsSuccess(request)
    }
  }
}

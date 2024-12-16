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
import uk.gov.hmrc.merchandiseinbaggage.model.core.PurchaseDetailsInput
import uk.gov.hmrc.merchandiseinbaggage.stubs.PayApiStub.{aImportGoods, completedImportGoods}

class DeclarationGoodsSpec extends AnyWordSpec with Matchers {

  "ImportGoods" should {
    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(aImportGoods) shouldBe Json.obj(
          "category"        -> "wine",
          "goodsVatRate"    -> "Twenty",
          "producedInEu"    -> "Yes",
          "purchaseDetails" -> Json.obj(
            "amount"   -> "99.99",
            "currency" -> Json.obj(
              "code"               -> "EUR",
              "displayName"        -> "title.euro_eur",
              "currencySynonyms"   -> Json.arr("Europe", "European"),
              "valueForConversion" -> "EUR"
            )
          )
        )
      }
      "deserialize to JSON" when {
        "all fields are valid" in {
          val json = Json.obj(
            "category"        -> "wine",
            "goodsVatRate"    -> "Twenty",
            "producedInEu"    -> "Yes",
            "purchaseDetails" -> Json.obj(
              "amount"   -> "99.99",
              "currency" -> Json.obj(
                "code"               -> "EUR",
                "displayName"        -> "title.euro_eur",
                "currencySynonyms"   -> Json.arr("Europe", "European"),
                "valueForConversion" -> "EUR"
              )
            )
          )
          json.validate[ImportGoods] shouldBe JsSuccess(aImportGoods)
        }
        "catergory is not defined" in {
          val json = Json.obj(
            "category"        -> "",
            "goodsVatRate"    -> "Twenty",
            "producedInEu"    -> "Yes",
            "purchaseDetails" -> Json.obj(
              "amount"   -> "99.99",
              "currency" -> Json.obj(
                "code"               -> "EUR",
                "displayName"        -> "title.euro_eur",
                "currencySynonyms"   -> Json.arr("Europe", "European"),
                "valueForConversion" -> "EUR"
              )
            )
          )
          json.validate[ImportGoods] shouldBe JsSuccess(
            ImportGoods(
              "",
              completedImportGoods.maybeGoodsVatRate.get,
              completedImportGoods.maybeProducedInEu.get,
              completedImportGoods.maybePurchaseDetails.get
            )
          )
        }
        "an extra field is valid" in {
          val json = Json.obj(
            "category"        -> "wine",
            "extra"           -> true,
            "goodsVatRate"    -> "Twenty",
            "producedInEu"    -> "Yes",
            "purchaseDetails" -> Json.obj(
              "amount"   -> "99.99",
              "currency" -> Json.obj(
                "code"               -> "EUR",
                "displayName"        -> "title.euro_eur",
                "currencySynonyms"   -> Json.arr("Europe", "European"),
                "valueForConversion" -> "EUR"
              )
            )
          )
          json.validate[ImportGoods] shouldBe JsSuccess(aImportGoods)
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
}

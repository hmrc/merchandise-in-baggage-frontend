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

class CurrencySpec extends AnyWordSpec with Matchers {
  val currency = Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))

  "Currency" should {

    "serialize to JSON" when {
      "the Email value is valid" in {
        Json.toJson(currency) shouldBe Json.obj(
          "code"               -> "EUR",
          "displayName"        -> "title.euro_eur",
          "currencySynonyms"   -> Json.arr("Europe", "European"),
          "valueForConversion" -> "EUR"
        )
      }
      "currencySynonyms is empty" in {
        Json.toJson(Currency("EUR", "title.euro_eur", Some("EUR"), List())) shouldBe Json.obj(
          "code"               -> "EUR",
          "displayName"        -> "title.euro_eur",
          "currencySynonyms"   -> Json.arr(),
          "valueForConversion" -> "EUR"
        )
      }
      "valueForConversion is not present" in {
        Json.toJson(Currency("EUR", "title.euro_eur", None, List("Europe", "European"))) shouldBe Json.obj(
          "code"             -> "EUR",
          "displayName"      -> "title.euro_eur",
          "currencySynonyms" -> Json.arr("Europe", "European")
        )
      }
    }

    "deserialize from JSON" when {
      "the JSON contains a valid Email value" in {
        val json = Json.obj(
          "code"               -> "EUR",
          "displayName"        -> "title.euro_eur",
          "currencySynonyms"   -> Json.arr("Europe", "European"),
          "valueForConversion" -> "EUR"
        )
        json.validate[Currency] shouldBe JsSuccess(currency)
      }
      "an extra field is present" in {
        val json = Json.obj(
          "extra"              -> 0,
          "code"               -> "EUR",
          "displayName"        -> "title.euro_eur",
          "currencySynonyms"   -> Json.arr("Europe", "European"),
          "valueForConversion" -> "EUR"
        )
        json.validate[Currency] shouldBe JsSuccess(currency)
      }
      "currencySynonyms is empty" in {
        val json = Json.obj(
          "code"               -> "EUR",
          "displayName"        -> "title.euro_eur",
          "currencySynonyms"   -> Json.arr(),
          "valueForConversion" -> "EUR"
        )
        json.validate[Currency] shouldBe JsSuccess(Currency("EUR", "title.euro_eur", Some("EUR"), List()))
      }
      "valueForConversion is not present" in {
        val json = Json.obj(
          "code"             -> "EUR",
          "displayName"      -> "title.euro_eur",
          "currencySynonyms" -> Json.arr("Europe", "European")
        )
        json.validate[Currency] shouldBe JsSuccess(Currency("EUR", "title.euro_eur", None, List("Europe", "European")))
      }
    }
    "fail to deserialize" when {
      "invalid JSON structure" in {
        val json = Json.arr(
          Json.obj("key" -> "value")
        )
        json.validate[Currency] shouldBe a[JsError]
      }
      "an empty JSON object" in {
        val json = Json.obj()
        json.validate[Currency] shouldBe a[JsError]
      }
    }
  }
}

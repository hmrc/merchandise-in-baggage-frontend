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

class TpsNavigationSpec extends AnyWordSpec with Matchers with CoreTestData {

  "tpsNavigation" should {
    val tpsNavigation: TpsNavigation = new TpsNavigation("a", "b", "c")
    "Serialize to json" when {
      "all the fields are present" in {
        Json.toJson(tpsNavigation) shouldBe Json.obj(
          "back"   -> "a",
          "reset"  -> "b",
          "finish" -> "c"
        )
      }
      "one of the fields are empty" in {
        Json.toJson(new TpsNavigation("a", "b", "")) shouldBe Json.obj(
          "back"   -> "a",
          "reset"  -> "b",
          "finish" -> ""
        )
      }
      "all the fields are empty" in {
        Json.toJson(new TpsNavigation("", "", "")) shouldBe Json.obj(
          "back"   -> "",
          "reset"  -> "",
          "finish" -> ""
        )
      }
    }
    "Deserialize to json" when {
      "all the fields are present" in {
        val json = Json.obj(
          "back"   -> "a",
          "reset"  -> "b",
          "finish" -> "c"
        )
        json.validate[TpsNavigation] shouldBe JsSuccess(tpsNavigation)
      }
      "none of the fields are present" in {
        val json = Json.obj()
        json.validate[TpsNavigation] shouldBe a[JsError]
      }
      "When one of the fields are empty" in {
        val json = Json.obj(
          "back"  -> "a",
          "reset" -> "b"
        )
        json.validate[TpsNavigation] shouldBe a[JsError]
      }
      "When there is an extra field" in {
        val json = Json.obj(
          "back"   -> "a",
          "reset"  -> "b",
          "finish" -> "c",
          "Extra"  -> true
        )
        json.validate[TpsNavigation] shouldBe JsSuccess(tpsNavigation)
      }
    }
  }
}

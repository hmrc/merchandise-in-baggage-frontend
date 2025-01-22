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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.*

class YesNoSpec extends AnyWordSpec with Matchers {

  "YesNo Enum" should {

    "contain Yes and No values" in {
      YesNo.values should contain allOf (Yes, No)
    }

    "correctly map from Boolean to YesNo" when {
      "Boolean is true" in {
        YesNo.from(true) shouldBe Yes
      }

      "Boolean is false" in {
        YesNo.from(false) shouldBe No
      }
    }

    "serialize to JSON" when {
      "value is Yes" in {
        Json.toJson(Yes) shouldBe JsString("Yes")
      }

      "value is No" in {
        Json.toJson(No) shouldBe JsString("No")
      }
    }

    "deserialize from JSON" when {
      "value is 'Yes'" in {
        JsString("Yes").validate[YesNo] shouldBe JsSuccess(Yes)
      }

      "value is 'No'" in {
        JsString("No").validate[YesNo] shouldBe JsSuccess(No)
      }

      "value is invalid" in {
        JsString("Maybe").validate[YesNo] shouldBe a[JsError]
      }
    }

    "round-trip serialization/deserialization" in {
      val yesNoValues = Seq(Yes, No)

      yesNoValues.foreach { value =>
        val json = Json.toJson(value)
        json.validate[YesNo] shouldBe JsSuccess(value)
      }
    }

    "return correct string representation" in {
      Yes.entryName shouldBe "Yes"
      No.entryName  shouldBe "No"
    }
  }
}

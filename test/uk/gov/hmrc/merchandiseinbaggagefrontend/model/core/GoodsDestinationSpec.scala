/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.core

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpec

class GoodsDestinationSpec extends BaseSpec with ScalaCheckPropertyChecks with OptionValues {

  "goodsDestination" must {
    "deserialise valid values" in {

      val gen = Gen.oneOf(GoodsDestination.values)

      forAll(gen) { goodsDestination =>
        JsString(goodsDestination.toString).validate[GoodsDestination].asOpt.value mustEqual goodsDestination
      }
    }

    "fail to deserialise invalid values" in {
      val gen = arbitrary[String] suchThat (!GoodsDestination.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[GoodsDestination] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = Gen.oneOf(GoodsDestination.values)

      forAll(gen) { goodsDestination =>
        Json.toJson(goodsDestination) mustEqual JsString(goodsDestination.toString)
      }
    }
  }

}

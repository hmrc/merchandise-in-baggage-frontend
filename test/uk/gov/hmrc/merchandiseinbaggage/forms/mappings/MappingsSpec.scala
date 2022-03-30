/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.forms.mappings

import enumeratum.EnumEntry
import org.scalatest.OptionValues
import play.api.data.{Form, FormError}
import uk.gov.hmrc.merchandiseinbaggage.BaseSpec
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.MappingsSpec.Foos.Bar
import uk.gov.hmrc.merchandiseinbaggage.model.api.Enum

import scala.collection.immutable

object MappingsSpec {

  sealed trait Foo extends EnumEntry

  object Foos extends Enum[Foo] {
    override val baseMessageKey: String = "foo"
    override val values: immutable.IndexedSeq[Foo] = findValues

    case object Bar extends Foo
    case object Baz extends Foo
  }
}

class MappingsSpec extends BaseSpec with OptionValues with Mappings {

  import MappingsSpec._

  "enumerable" must {

    val testForm = Form(
      "value" -> enum[Foo](Foos)
    )

    "bind a valid option" in {
      val result = testForm.bind(Map("value" -> "Bar"))
      result.get mustEqual Bar
    }

    "not bind an invalid option" in {
      val result = testForm.bind(Map("value" -> "Not Bar"))
      result.errors must contain(FormError("value", "error.invalid"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }
  }
}

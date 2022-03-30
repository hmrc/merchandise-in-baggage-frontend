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

package uk.gov.hmrc.merchandiseinbaggage.forms.behaviours

import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}
import uk.gov.hmrc.merchandiseinbaggage.forms.FormSpec
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.LocalDateFormatter.{dayBlankErrorKey, monthBlankErrorKey, yearBlankErrorKey}
import uk.gov.hmrc.merchandiseinbaggage.generators.Generators

trait FieldBehaviours extends FormSpec with ScalaCheckPropertyChecks with Generators {

  def mandatoryField(form: Form[_], fieldName: String, requiredError: FormError): Unit = {
    s"not bind when key is not present at all with message $requiredError" in {
      val result = form.bind(emptyForm).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    s"not bind blank values with message $requiredError" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

  def mandatoryEmailField(form: Form[_], fieldName: String, requiredError: FormError): Unit = {

    "not bind when key is not present at all" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind blank values" in {

      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual Seq(FormError(fieldName, "enterEmail.error.required"))
    }
  }

  def optionalEmailFieldIfAssistedDigital(form: Form[_], fieldName: String): Unit = {
    "bind when key is present" in {
      val result = form.bind(Map(fieldName -> "xx@yyy")).apply(fieldName)
      result.errors mustEqual Seq()
    }

    "not bind blank values" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual Seq()
    }
  }

  def aMandatoryDateField(form: Form[_], key: String): Unit =
    "fail to bind an empty date" in {
      val result = form.bind(Map.empty[String, String])

      result.errors must contain allElementsOf List(
        FormError(s"$key.day", dayBlankErrorKey),
        FormError(s"$key.month", monthBlankErrorKey),
        FormError(s"$key.year", yearBlankErrorKey),
      )
    }
}

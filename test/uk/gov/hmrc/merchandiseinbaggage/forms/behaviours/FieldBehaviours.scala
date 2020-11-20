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

package uk.gov.hmrc.merchandiseinbaggage.forms.behaviours

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

import enumeratum.EnumEntry
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}
import uk.gov.hmrc.merchandiseinbaggage.forms.FormSpec
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.LocalDateFormatter.{dayBlankErrorKey, monthBlankErrorKey, yearBlankErrorKey}
import uk.gov.hmrc.merchandiseinbaggage.generators.Generators
import uk.gov.hmrc.merchandiseinbaggage.model.Enum

trait FieldBehaviours extends FormSpec with ScalaCheckPropertyChecks with Generators {

  def fieldThatBindsValidData(form: Form[_], fieldName: String, validDataGenerator: Gen[String]): Unit =
    "bind valid data" in {

      forAll(validDataGenerator -> "validDataItem") { dataItem: String =>
        val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
        result.value.value mustBe dataItem
      }
    }

  def mandatoryField(form: Form[_], fieldName: String, requiredError: FormError): Unit = {

    "not bind when key is not present at all" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind blank values" in {

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
      result.errors mustEqual Seq(FormError(fieldName, "error.email"))
    }
  }

  def anEnumField(form: Form[_], fieldName: String, enum: Enum[_ <: EnumEntry], invalidMessageKey: String): Unit = {
    "bind all valid values" in {
      for (value <- enum.values) {
        form.bind(Map(fieldName -> value.entryName)).apply(fieldName).value.value mustEqual value.entryName
      }
    }

    "not bind invalid values" in {
      val generator = stringsExceptSpecificValues(enum.values.map(_.entryName))

      forAll(generator -> "invalidValue") { value =>
        form.bind(Map(fieldName -> value)).apply(fieldName).errors mustEqual Seq(FormError(fieldName, invalidMessageKey))
      }
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

  def aDateFieldWithMin(form: Form[_], key: String, min: LocalDate, error: String): Unit =
    s"fail to bind a date earlier than ${min.format(DateTimeFormatter.ISO_LOCAL_DATE)}" in {

      val generator = datesBetween(min.minusYears(10), min.minusDays(1))

      forAll(generator -> "invalid dates") { date =>
        val data = Map(
          s"$key.day" -> date.getDayOfMonth.toString,
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year" -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors.contains(FormError(key, error)) mustBe true
      }
    }

  def aDateFieldWithMax(form: Form[_], key: String, max: LocalDate, error: String): Unit =
    s"fail to bind a date greater than ${max.format(ISO_LOCAL_DATE)}" in {
      val generator = datesBetween(max.plusDays(1), max.plusYears(10))

      forAll(generator -> "invalid dates") { date =>
        val data = Map(
          s"$key.day" -> date.getDayOfMonth.toString,
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year" -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors.contains(FormError(key, error)) mustBe true
      }
    }
}

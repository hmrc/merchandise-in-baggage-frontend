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

package uk.gov.hmrc.merchandiseinbaggage.model.core

import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.{JsBoolean, JsError, JsString, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.Email
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney.*
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

import java.time.{LocalDate, LocalDateTime}

class DeclarationJourneySpec extends BaseSpec with CoreTestData with PropertyBaseTables {

  forAll(declarationTypesTable) { declarationType =>
    forAll(journeyTypesTable) { journeyType =>
      s"instantiate a declaration journey for $declarationType $journeyType" in {
        val sessionId = aSessionId
        val actual    = DeclarationJourney(sessionId, declarationType, isAssistedDigital = false, journeyType)
        actual.sessionId mustBe sessionId
        actual.declarationType mustBe declarationType
        actual.journeyType mustBe journeyType
        if (declarationType == Import) actual.goodsEntries mustBe GoodsEntries(ImportGoodsEntry())
        if (declarationType == Export) actual.goodsEntries mustBe GoodsEntries(ExportGoodsEntry())
      }
    }
  }

  "fail to deserialize DeclarationJourney" when {
    "invalid JSON structure" in {
      val json = Json.arr(
        Json.obj("key" -> "value")
      )
      json.validate[DeclarationJourney] shouldBe a[JsError]
    }
    "an empty JSON object" in {
      val json = Json.obj()
      json.validate[DeclarationJourney] shouldBe a[JsError]
    }
  }

  "fail to deserialize GoodsEntries" when {
    "invalid JSON structure" in {
      val json = Json.arr(
        Json.obj("key" -> "value")
      )
      json.validate[GoodsEntries] shouldBe a[JsError]
    }
    "an empty JSON object" in {
      val json = Json.obj()
      json.validate[GoodsEntries] shouldBe a[JsError]
    }
  }

  "return empty email as default" in {
    defaultEmail(isAssistedDigital = true, Some(Email("x@y"))) mustBe Some(Email(""))
    defaultEmail(isAssistedDigital = false, Some(Email("x@y"))) mustBe Some(Email("x@y"))
  }

  "set user email" in {
    userEmail(isAssistedDigital = true, Some(Email("x@y")), Email("zz@y")) mustBe Some(Email("x@y"))
    userEmail(isAssistedDigital = false, Some(Email("x@y")), Email("zz@y")) mustBe Some(Email("zz@y"))
  }

  val date                             = LocalDate.of(2018, 2, 1).atStartOfDay
  val dateTimeWithoutZ                 = LocalDateTime.of(2018, 2, 1, 14, 30, 30, 500)
  val dateTimeWithZ                    = LocalDateTime.of(2018, 2, 1, 14, 30, 30, 500).toString + "Z"
  val dateString                       = date.toString
  val dateStringWithZ                  = dateTimeWithZ.toString
  val dateMillis                       = 1517443200000L
  val dateMillisBigDecimal: BigDecimal = 1517443200000L
  val jsonMillis                       = Json.obj(s"$$date" -> dateMillis)
  val jsonBigDecimal                   = Json.obj(s"$$date" -> dateMillisBigDecimal)
  val json                             = Json.obj(s"$$date" -> date)

  "parseDateString" should {
    "convert a zonedDateString to a dateTime" in {
      val result = parseDateString("2018-02-01T14:30:30.000000500Z")
      result mustEqual JsSuccess(LocalDateTime.of(2018, 2, 1, 14, 30, 30, 500))
    }
    "convert a localDateString to a dateTime" in {
      val result = parseDateString("2018-02-01T14:30:30.000000500")
      result mustEqual JsSuccess(LocalDateTime.of(2018, 2, 1, 14, 30, 30, 500))
    }
    "return an error when a string cannot convert to a date" in {
      val result = parseDateString("Cat")
      result mustEqual JsError("Unexpected LocalDateTime Format")
    }
  }

  "localDateTimeRead" should {
    "decode a decimal to a dateTime" in {
      val result = localDateTimeRead.reads(jsonBigDecimal)
      result mustEqual JsSuccess(LocalDate.of(2018, 2, 1).atStartOfDay)
    }
    "decode a long to a dateTime" in {
      val result = localDateTimeRead.reads(jsonMillis)
      result mustEqual parseDateString(LocalDate.of(2018, 2, 1).atStartOfDay.toString)
    }
    "decode a string of LocalDateTime to a dateTime" in {
      val result = localDateTimeRead.reads(JsString(dateString))
      result mustEqual parseDateString(LocalDate.of(2018, 2, 1).atStartOfDay.toString)
    }
    "decode a string of ZonedDateTime to a dateTime" in {
      val result = localDateTimeRead.reads(JsString(dateStringWithZ))
      result mustEqual parseDateString(LocalDateTime.of(2018, 2, 1, 14, 30, 30, 500).toString + "Z")
    }
    "decode some json to a dateTime" in {
      val result = localDateTimeRead.reads(jsonMillis)
      result mustEqual JsSuccess(LocalDate.of(2018, 2, 1).atStartOfDay)
    }
    "not decode a random string" in {
      val result = localDateTimeRead.reads(JsString("abc"))
      result mustEqual JsError("Unexpected LocalDateTime Format")
    }
    "not decode null" in {
      val result = localDateTimeRead.reads(null)
      result mustEqual JsError("Unexpected LocalDateTime Format")
    }
    "not decode an boolean" in {
      val result = localDateTimeRead.reads(JsBoolean(true))
      result mustEqual JsError("Unexpected LocalDateTime Format")
    }
    "not decode an empty string" in {
      val result = localDateTimeRead.reads(JsString(""))
      result mustEqual JsError("Unexpected LocalDateTime Format")
    }
    "not decode an null string" in {
      val result = localDateTimeRead.reads(JsString(null))
      result mustEqual JsError("Unexpected LocalDateTime Format")
    }
    "not decode invalid JSON structure" in {
      val result = localDateTimeRead.reads(
        Json.arr(
          Json.obj("key" -> "value")
        )
      )
      result mustEqual JsError("Unexpected LocalDateTime Format")
    }
    "not decode an empty JSON object" in {
      val result = localDateTimeRead.reads(Json.obj())
      result mustEqual JsError("Unexpected LocalDateTime Format")
    }
  }
}

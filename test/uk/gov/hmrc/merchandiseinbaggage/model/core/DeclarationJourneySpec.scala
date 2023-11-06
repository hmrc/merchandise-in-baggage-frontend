/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{JsError, JsString, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.Email
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

import java.time.{LocalDate, LocalDateTime}

class DeclarationJourneySpec extends BaseSpec with CoreTestData with PropertyBaseTables {

  forAll(declarationTypesTable) { declarationType =>
    forAll(journeyTypesTable) { journeyType =>
      s"instantiate a declaration journey for $declarationType $journeyType" in {
        val sessionId = aSessionId
        val actual    = DeclarationJourney(sessionId, declarationType, journeyType)
        actual.sessionId mustBe sessionId
        actual.declarationType mustBe declarationType
        actual.journeyType mustBe journeyType
        if (declarationType == Import) actual.goodsEntries mustBe GoodsEntries(ImportGoodsEntry())
        if (declarationType == Export) actual.goodsEntries mustBe GoodsEntries(ExportGoodsEntry())
      }
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
  }
}

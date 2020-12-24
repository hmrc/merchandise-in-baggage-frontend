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

package uk.gov.hmrc.merchandiseinbaggage.utils

import play.api.i18n.Messages

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import java.util.Locale

object DateUtils {

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM YYYY, h:mm a", Locale.ENGLISH)
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)

   def translatedDate(date: String)(implicit messages: Messages): String = {
    if (messages.lang.code == "cy") {
      val englishMonth = date.split(" ")(1)
      date.replace(englishMonth, messages(s"title.${englishMonth.toLowerCase}"))
    } else
      date
  }

  implicit class LocalDateTimeOps(dateTime: LocalDateTime) {
    def formattedDate(implicit messages: Messages): String = {
      val dateFormatted = dateTime.format(dateTimeFormatter)
      translatedDate(dateFormatted)
    }
  }

  implicit class LocalDateOps(date: LocalDate) {
    def formattedDate(implicit messages: Messages): String = {
      val dateFormatted = date.format(dateFormatter)
      translatedDate(dateFormatted)
    }
  }
}

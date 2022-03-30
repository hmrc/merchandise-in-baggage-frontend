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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import enumeratum.EnumEntry
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait JourneyType extends EnumEntry with EnumEntryRadioItemSupport {
  val messageKey = s"${JourneyTypes.baseMessageKey}.$entryName"
}

object JourneyType {
  implicit val format: Format[JourneyType] = EnumFormat(JourneyTypes)
}

object JourneyTypes extends Enum[JourneyType] with RadioSupport[JourneyType] {
  override val baseMessageKey = "journeyType"
  override val values: immutable.IndexedSeq[JourneyType] = findValues

  case object New extends JourneyType
  case object Amend extends JourneyType
}

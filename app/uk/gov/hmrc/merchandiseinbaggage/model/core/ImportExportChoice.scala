/*
 * Copyright 2021 HM Revenue & Customs
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

import enumeratum.EnumEntry
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Enum, EnumEntryRadioItemSupport, RadioSupport}

import scala.collection.immutable

sealed trait ImportExportChoice extends EnumEntry with EnumEntryRadioItemSupport {
  val messageKey = s"${ImportExportChoices.baseMessageKey}.$entryName"
}

object ImportExportChoices extends Enum[ImportExportChoice] with RadioSupport[ImportExportChoice] {
  override val baseMessageKey = "importExportChoice"
  override val values: immutable.IndexedSeq[ImportExportChoice] = findValues

  case object MakeImport extends ImportExportChoice
  case object MakeExport extends ImportExportChoice
  case object AddToExisting extends ImportExportChoice
}

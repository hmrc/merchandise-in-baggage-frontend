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

package uk.gov.hmrc.merchandiseinbaggage.generators

import org.scalatest.prop.{TableFor1, TableFor2}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, JourneyType, YesNo}

trait PropertyBaseTables extends ScalaCheckPropertyChecks {

  val declarationTypesTable: TableFor1[DeclarationType] = Table("declarationType", Import, Export)

  val journeyTypesTable: TableFor1[JourneyType] = Table("journeyType", New, Amend)

  val traderYesOrNoAnswer: TableFor2[YesNo, String] = Table(
    ("answer", "trader or agent"),
    (Yes, "agent"),
    (No, "trader")
  )
}

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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class AmendmentSpec extends BaseSpec with CoreTestData {

  "populate the source to AssistedDigital if internal frontend" in {
    val stub = new JourneySourceFinder {
      override def findSource: Option[String] = Some("AssistedDigital")
    }
    Amendment(
      1,
      LocalDateTime.now.truncatedTo(ChronoUnit.MILLIS),
      aDeclarationGood,
      source = stub.findSource
    ).source mustBe Some("AssistedDigital")
  }

  "populate the source to Digital if public facing" in new JourneySourceFinder {
    override lazy val isAssistedDigital: Boolean = false
    Amendment(1, LocalDateTime.now.truncatedTo(ChronoUnit.MILLIS), aDeclarationGood).source mustBe Some("Digital")
  }
}

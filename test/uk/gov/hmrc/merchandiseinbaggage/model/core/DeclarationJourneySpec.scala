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

import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.Email
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class DeclarationJourneySpec extends BaseSpec with CoreTestData with PropertyBaseTables {

  forAll(declarationTypesTable) { declarationType =>
    forAll(journeyTypesTable) { journeyType =>
      s"instantiate a declaration journey for $declarationType $journeyType" in {
        val sessionId = aSessionId
        val actual = DeclarationJourney(sessionId, declarationType, journeyType)
        actual.sessionId mustBe sessionId
        actual.declarationType mustBe declarationType
        actual.journeyType mustBe journeyType
        if (declarationType == Import) actual.goodsEntries mustBe GoodsEntries(ImportGoodsEntry())
        if (declarationType == Export) actual.goodsEntries mustBe GoodsEntries(ExportGoodsEntry())
      }
    }
  }

  "return empty email as default" in {
    defaultEmail(true, Some(Email("x@y"))) mustBe Some(Email(""))
    defaultEmail(false, Some(Email("x@y"))) mustBe Some(Email("x@y"))
  }

  "set user email" in {
    userEmail(true, Some(Email("x@y")), Email("zz@y")) mustBe Some(Email("x@y"))
    userEmail(false, Some(Email("x@y")), Email("zz@y")) mustBe Some(Email("zz@y"))
  }
}

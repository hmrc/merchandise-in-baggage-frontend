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

package uk.gov.hmrc.merchandiseinbaggage.utils

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.New
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.{Address, AddressLookupCountry}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Email, Eori, Name}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.utils.Obfuscate._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class ObfuscateSpec extends BaseSpec with CoreTestData {
  "obfuscate should" should {
    "return a string of `*` characters of the same length as the input" in {
      "1".obfuscated mustBe "*"
      "12".obfuscated mustBe "**"
    }

    "return an empty string" when {
      "the input is empty" in {
        "".obfuscated mustBe ""
      }
    }
  }

  "maybeObfuscate" should {
    "obfuscate a defined option" in {
      optionStringObfuscate.obfuscated(Some("1")) mustBe Some("*")
    }

    "tolerate an undefined option" in {
      optionStringObfuscate.obfuscated(None) mustBe None
    }
  }

  "obfuscate a declaration journey" in {
    import completedDeclarationJourney._
    val journey = completedDeclarationJourney.copy(sessionId = aSessionId)
    journey.obfuscated mustBe
      DeclarationJourney(
        aSessionId,
        Import,
        New,
        createdAt,
        maybeExciseOrRestrictedGoods,
        maybeGoodsDestination,
        maybeValueWeightOfGoodsBelowThreshold,
        goodsEntries,
        Some(Name("*****", "****")),
        Some(Email("***********")),
        maybeIsACustomsAgent,
        Some("**********"),
        Some(Address(List("*************", "**********"), Some("*******"), AddressLookupCountry("**", Some("**************")))),
        Some(Eori("**************")),
        maybeJourneyDetailsEntry,
        maybeTravellingByVehicle,
        maybeTravellingBySmallVehicle,
        Some("******"),
        declarationId
      )
  }
}

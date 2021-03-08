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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.mvc.Call
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, JourneyType}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney

class NavigatorSpec extends DeclarationJourneyControllerSpec with PropertyBaseTables {

  forAll(declarationTypesTable) { importOrExport: DeclarationType =>
    forAll(journeyTypesTable) { newOrAmend: JourneyType =>
      val journey: DeclarationJourney = DeclarationJourney(aSessionId, importOrExport).copy(journeyType = newOrAmend)

      s"On ${ExciseAndRestrictedGoodsController.onPageLoad().url}" must {
        s"redirect to ${CannotUseServiceController.onPageLoad().url} if submit with Yes for $importOrExport and $newOrAmend" in new Navigator {
          val result: Call = nextPage(ExciseAndRestrictedGoodsController.onPageLoad().url, Yes, journey, Some(1))

          result.url mustBe CannotUseServiceController.onPageLoad().url
        }

        if (newOrAmend == Amend) {
          s"redirect to ${GoodsTypeQuantityController.onPageLoad(1).url} for $newOrAmend on submit for $importOrExport" in new Navigator {
            val result: Call = nextPage(ExciseAndRestrictedGoodsController.onPageLoad().url, No, journey, Some(1))

            result.url mustBe GoodsTypeQuantityController.onPageLoad(1).url
          }
        }

        if (newOrAmend == New) {
          s"redirect to ${GoodsTypeQuantityController.onPageLoad(1).url} for $newOrAmend on submit for $importOrExport" in new Navigator {
            val result: Call = nextPage(ExciseAndRestrictedGoodsController.onPageLoad().url, No, journey, Some(1))

            result.url mustBe ValueWeightOfGoodsController.onPageLoad().url
          }
        }
      }
    }
  }
}

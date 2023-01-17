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

package uk.gov.hmrc.merchandiseinbaggage.content

import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.GoodsTypePage

class GoodTypeContentSpec extends GoodsTypePage with CoreTestData with PropertyBaseTables {

  forAll(declarationTypesTable) { importOrExport =>
    forAll(journeyTypesTable) { newOrAmend =>
      s"render contents for $importOrExport & $newOrAmend" in {
        givenAJourneyWithSession(declarationType = importOrExport, journeyType = newOrAmend)
        goToGoodsTypePage(1, newOrAmend)

        elementText(findByTagName("label")) must include(messages("goodsType.category"))
        elementText(findById("category-hint")) must include(messages("goodsType.category.hint"))
      }
    }
  }
}

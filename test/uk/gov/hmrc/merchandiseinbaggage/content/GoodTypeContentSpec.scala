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

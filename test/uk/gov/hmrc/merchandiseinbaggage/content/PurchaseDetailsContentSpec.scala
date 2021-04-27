package uk.gov.hmrc.merchandiseinbaggage.content

import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.PurchaseDetailsPage

class PurchaseDetailsContentSpec extends PurchaseDetailsPage with CoreTestData with PropertyBaseTables {

  s"render contents" in {
    givenAJourneyWithSession()
    goToPurchaseDetailsPage(1)

    elementText(findByTagName("h1")) must include(messages("purchaseDetails.heading", "wine"))
    elementText(findByClassName("govuk-inset-text")) must include(messages("purchaseDetails.type.of.goods", "wine"))
  }
}

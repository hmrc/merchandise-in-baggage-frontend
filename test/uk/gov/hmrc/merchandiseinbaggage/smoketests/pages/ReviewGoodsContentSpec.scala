package uk.gov.hmrc.merchandiseinbaggage.smoketests.pages

import java.util

import org.openqa.selenium.{By, WebElement}
import org.scalatest.Assertion
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.ReviewGoodsPage._

class ReviewGoodsContentSpec extends ReviewGoodsPage with CoreTestData with PropertyBaseTables {

    s"render content" in {
      givenAJourneyWithSession()
      goToReviewGoodsPagePage

      pageTitle mustBe title
      elementText(findByTagName("h1")) must include(messages("reviewGoods.heading"))
      elementText(findByTagName("h2")) must include(messages("reviewGoods.h3"))

      val rows = findById("summaryListId").findElements(By.className("govuk-summary-list__row"))

      rowTest(rows, 0, "reviewGoods.list.item", "wine", "/goods-type-quantity/1")
      rowTest(rows, 1, "reviewGoods.list.quantity", "1", "/goods-type-quantity/1")
      rowTest(rows, 2, "reviewGoods.list.vatRate", "20%", "/goods-vat-rate/1")
      rowTest(rows, 3, "reviewGoods.list.producedInEu", "Yes", "/goods-origin/1")
      rowTest(rows, 4, "reviewGoods.list.price", "99.99, Euro (EUR)", "/purchase-details/1")

      findByTagName("a").getAttribute("href") must include("review-goods#main-content")
  }

  private def rowTest(rows: util.List[WebElement], rowNumber: Int, key: String, value: String, changeLink: String): Assertion = {
    rows.get(rowNumber).findElement(By.className("govuk-summary-list__key")).getText mustBe messages(key)
    rows.get(rowNumber).findElement(By.className("govuk-summary-list__value")).getText mustBe value
    rows.get(rowNumber).findElement(By.className("govuk-summary-list__actions"))
      .findElement(By.className("govuk-link")).getAttribute("href") must include(changeLink)
  }
}

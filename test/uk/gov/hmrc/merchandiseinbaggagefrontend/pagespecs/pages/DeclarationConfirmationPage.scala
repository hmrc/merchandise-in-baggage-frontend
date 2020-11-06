/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages

import java.time.LocalDateTime

import org.openqa.selenium.WebDriver
import org.scalatest.Assertion
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{Declaration, Goods}

class DeclarationConfirmationPage(implicit webDriver: WebDriver) extends BasePage {

  def hasConfirmationPanelWithContents: Assertion = {
    attrOfElementWithId("confirmationPanelId", "class") mustBe "govuk-panel govuk-panel--confirmation"
    textOfElementWithId("panelTitleId") mustBe "Declaration complete"
    textOfElementWithId("mibReferenceId") must include("Your reference number")
  }

  def hasDateOfDeclaration: Assertion = {
    textOfElementWithId("declarationDateId") mustBe "Date of declaration"
    textOfElementWithId("declarationDateFormattedId") must include(LocalDateTime.now.format(Declaration.formatter))
  }

  def hasPrintPageContentInPdf: Assertion = {
    attrOfElementWithId("printDeclarationId", "href") mustBe "javascript:window.print();"
    textOfElementWithId("printDeclarationId") mustBe "Print or save a copy of this page"
    attrOfElementWithId("printDeclarationLinkId", "href") must include("/merchandise-in-baggage/assets/stylesheets/application.css")
    attrOfElementWithId("printDeclarationLinkId", "media") mustBe "all"
  }

  def hasWhaToDoNext: Assertion = {
    textOfElementWithId("whatToDoNextId") mustBe "What you need to do next"
    val listItems = unifiedListItemsById("whatToDoNextUlId")
    listItems.get(0).getText mustBe "take this declaration confirmation with you"
    listItems.get(1).getText mustBe "take the invoices for all the goods you are taking out of the UK"
  }

  def hasGoodDetails(declaration: Declaration): Assertion = {
    val withIndex: Seq[(Goods, Int)] = declaration.declarationGoods.goods.zipWithIndex

    withIndex map {
      case (good, idx) =>
        textOfElementWithId(s"categoryLabel_$idx") mustBe "Type of goods"
        textOfElementWithId(s"category_$idx") mustBe good.categoryQuantityOfGoods.category
        textOfElementWithId(s"quantityLabel_$idx") mustBe "Number of items"
        textOfElementWithId(s"quantity_$idx") mustBe good.categoryQuantityOfGoods.quantity
        textOfElementWithId(s"countryLabel_$idx") mustBe "Country"
        textOfElementWithId(s"country_$idx") mustBe good.countryOfPurchase
        textOfElementWithId(s"priceLabel_$idx") mustBe "Price paid"
        textOfElementWithId(s"price_$idx") mustBe good.purchaseDetails.toString
        textOfElementWithId(s"invoiceNumberLabel_$idx") mustBe "Invoice number"
        textOfElementWithId(s"invoiceNumber_$idx") mustBe good.invoiceNumber
    }
    textOfElementWithId("goodsDetailsId") mustBe "Details of the goods"
  }
}

object DeclarationConfirmationPage {
  val path = "/merchandise-in-baggage/declaration-confirmation"
  val title = "Confirmation page"
}
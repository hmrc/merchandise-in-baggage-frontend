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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs

import java.time.LocalDateTime

import com.softwaremill.macwire.wire
import org.scalatest.Assertion
import uk.gov.hmrc.merchandiseinbaggage.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationId, DeclarationType, Goods}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.DeclarationConfirmationPage
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.DeclarationConfirmationPage._
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.utils.DateUtils._

class DeclarationConfirmationPageSpec extends BasePageSpec[DeclarationConfirmationPage] with WireMockSupport {
  override lazy val page: DeclarationConfirmationPage = wire[DeclarationConfirmationPage]
  import page._

  "the page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWithNoBackButton(path)

    "render correctly" when {
      "make declaration for export" in {
        val id = DeclarationId("456")
        val declarationJourney = completedDeclarationJourney.copy(declarationType = DeclarationType.Export)
        val declarationCompleted = declarationJourney.declarationIfRequiredAndComplete.get
        givenADeclarationJourney(declarationJourney.copy(declarationId = id))
        givenPersistedDeclarationIsFound(wireMockServer, declarationCompleted, id)
        open(path)

        page.mustRenderBasicContentWithoutHeader(path, title)
        hasConfirmationPanelWithContents
        hasDateOfDeclaration(declarationCompleted.dateOfDeclaration)
        hasEmailAddress(declarationCompleted)
        hasPrintPageContentInPdf
        hasWhaToDoNextExport
        hasGoodDetails(declarationCompleted)
        hasPersonDetails(declarationCompleted)
        hasMakeAnotherDeclarationLink
        hasFeedbackSurveyLink
      }

      "make declaration for import and given payment is success" in {
        val id = DeclarationId("456")
        val declarationJourney = completedDeclarationJourney.copy(declarationType = DeclarationType.Import)
        val declarationCompleted = declarationJourney.declarationIfRequiredAndComplete.get
        givenADeclarationJourney(declarationJourney.copy(declarationId = id))
        givenPersistedDeclarationIsFound(wireMockServer, declarationCompleted.copy(paymentSuccess = Some(true)), id)
        open(path)

        page.mustRenderBasicContentWithoutHeader(path, title)
        hasConfirmationPanelWithContents
        hasDateOfDeclaration(declarationCompleted.dateOfDeclaration)
        hasEmailAddress(declarationCompleted)
        hasPrintPageContentInPdf
        hasWhaToDoNextImport
        hasGoodDetails(declarationCompleted)
        hasPersonDetails(declarationCompleted)
        hasMakeAnotherDeclarationLink
        hasFeedbackSurveyLink
      }
    }
  }

  def hasConfirmationPanelWithContents: Assertion = {
    attrOfElementWithId("confirmationPanelId", "class") mustBe "govuk-panel govuk-panel--confirmation"
    textOfElementWithId("panelTitleId") mustBe "Declaration complete"
    textOfElementWithId("mibReferenceId") must include("Your reference number")
  }

  def hasDateOfDeclaration(ldt: LocalDateTime): Assertion = {
    textOfElementWithId("declarationDateId") mustBe "Date of declaration"
    textOfElementWithId("declarationDateFormattedId") must include(ldt.formattedDate)
  }

  def hasEmailAddress(declaration: Declaration): Assertion =
    textOfElementWithId("declarationEmailId") mustBe s"We have sent you a confirmation email to ${declaration.email.email}."

  def hasPrintPageContentInPdf: Assertion = {
    attrOfElementWithId("printDeclarationId", "href") mustBe "javascript:window.print();"
    textOfElementWithId("printDeclarationId") mustBe "Print or save a copy of this page"
    attrOfElementWithId("printDeclarationLinkId", "href") must include("/declare-commercial-goods/assets/stylesheets/application.css")
    attrOfElementWithId("printDeclarationLinkId", "media") mustBe "all"
  }

  def hasWhaToDoNextExport: Assertion = {
    textOfElementWithId("whatToDoNextId") mustBe "What you need to do next"
    val listItems = unifiedListItemsById("whatToDoNextUlId")
    listItems.get(0).getText mustBe "take this declaration confirmation with you"
    listItems.get(1).getText mustBe "take the receipts or invoices for all the goods you are taking out of Great Britain"
  }

  def hasWhaToDoNextImport: Assertion = {
    textOfElementWithId("whatToDoNextId") mustBe "What you need to do next"
    val listItems = unifiedListItemsById("whatToDoNextUlId")
    listItems.get(0).getText mustBe "go through the green channel (nothing to declare) at customs"
    listItems.get(1).getText mustBe "take this declaration confirmation with you"
    listItems.get(2).getText mustBe "take the receipts or invoices for all the goods you are bringing into Great Britain"
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
        textOfElementWithId(s"country_$idx") mustBe messages(good.countryOfPurchase.countryName)
        textOfElementWithId(s"priceLabel_$idx") mustBe "Price paid"
        textOfElementWithId(s"price_$idx") mustBe good.purchaseDetails.formatted
    }
    textOfElementWithId("goodsDetailsId") mustBe "Details of the goods"
  }

  def hasPersonDetails(declaration: Declaration): Assertion = {
    textOfElementWithId("personalDetailsId") mustBe "Personal details"
    textOfElementWithId("nameOfPersonCarryingTheGoodsLabel") mustBe "Name of person carrying goods"
    textOfElementWithId("nameOfPersonCarryingTheGoods") mustBe declaration.nameOfPersonCarryingTheGoods.toString
    textOfElementWithId("eoriLabel") mustBe "EORI number"
    textOfElementWithId("eori") mustBe declaration.eori.value
  }

  def hasMakeAnotherDeclarationLink: Assertion = {
    textOfElementWithId("makeAnotherDeclarationId") mustBe "Make another declaration"
    element("makeAnotherDeclarationId").getAttribute("href") must include("/declare-commercial-goods/goods-destination")
  }

  def hasFeedbackSurveyLink: Assertion = {
    textOfElementWithId("feedbackSurvey") mustBe "What did you think of this service?"
    element("feedbackSurvey").getAttribute("href") must include("/declare-commercial-goods/start-survey")
  }
}

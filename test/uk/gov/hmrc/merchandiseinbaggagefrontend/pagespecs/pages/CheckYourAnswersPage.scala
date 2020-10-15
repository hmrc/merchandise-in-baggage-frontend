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

import org.openqa.selenium.WebDriver
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{AmountInPence, Declaration}

class CheckYourAnswersPage(baseUrl: BaseUrl)(implicit webDriver: WebDriver) extends BasePage(baseUrl) {

  import WebBrowser._

  override val path = "/merchandise-in-baggage/check-your-answers"
  override val expectedTitle = "Check your answers before making your declaration"

  private val expectedSectionHeaders =
    Seq("Details of the goods", "Personal details", "Journey details", "Now send your declaration")

  def assertPageIsDisplayed(): Unit = patiently(ensureBasicContent())

  def assertDetailIsRendered(declaration: Declaration, totalTaxDue: AmountInPence): Unit = patiently {
    findAll(TagNameQuery("h2")).map(_.underlying.getText).toSeq.dropRight(1) mustBe expectedSectionHeaders

    declaration.declarationGoods.goods.zipWithIndex.foreach { goodsWithIndex =>
      val goods = goodsWithIndex._1
      val index = goodsWithIndex._2

      textOfElementWithId(s"categoryLabel_$index") mustBe "Type of goods"
      textOfElementWithId(s"category_$index") mustBe goods.categoryQuantityOfGoods.category

      textOfElementWithId(s"quantityLabel_$index") mustBe "Number of items"
      textOfElementWithId(s"quantity_$index") mustBe goods.categoryQuantityOfGoods.quantity

      textOfElementWithId(s"countryLabel_$index") mustBe "Country"
      textOfElementWithId(s"country_$index") mustBe goods.countryOfPurchase

      textOfElementWithId(s"priceLabel_$index") mustBe "Price paid"
      textOfElementWithId(s"price_$index") mustBe goods.purchaseDetails.toString

      textOfElementWithId(s"invoiceNumberLabel_$index") mustBe "Invoice number"
      textOfElementWithId(s"invoiceNumber_$index") mustBe goods.invoiceNumber
    }

    textOfElementWithId("taxDueLabel") mustBe "Tax due"
    textOfElementWithId("taxDueValue") mustBe totalTaxDue.formattedInPounds

    declaration.maybeCustomsAgent.fold {
      elementIsNotRenderedWithId("customsAgentNameLabel")
      elementIsNotRenderedWithId("customsAgentName")

      elementIsNotRenderedWithId("customsAgentAddressLabel")
      elementIsNotRenderedWithId("customsAgentAddress")
    } { customsAgent =>
      textOfElementWithId("customsAgentNameLabel") mustBe "Name of customs agent"
      textOfElementWithId("customsAgentName") mustBe customsAgent.name

      textOfElementWithId("customsAgentAddressLabel") mustBe "Customs agent address"
      textOfElementWithId("customsAgentAddress").contains(customsAgent.address.postCode) mustBe true
    }

    textOfElementWithId("eoriLabel") mustBe "EORI number"
    textOfElementWithId("eori") mustBe declaration.eori.toString

    textOfElementWithId("nameOfPersonCarryingTheGoodsLabel") mustBe "Name of person carrying goods"
    textOfElementWithId("nameOfPersonCarryingTheGoods") mustBe declaration.nameOfPersonCarryingTheGoods.toString

    textOfElementWithId("placeOfArrivalLabel") mustBe "Place of arrival"
    textOfElementWithId("placeOfArrival") mustBe declaration.journeyDetails.placeOfArrival.entryName

    textOfElementWithId("dateOfArrivalLabel") mustBe "Date of arrival"
    textOfElementWithId("dateOfArrival") mustBe declaration.journeyDetails.formattedDateOfArrival

    textOfElementWithId("travellingByVehicleLabel") mustBe "Travelling by vehicle"
    textOfElementWithId("travellingByVehicle") mustBe declaration.travellingByVehicle.entryName

    declaration.travellingByVehicle match {
      case No =>
        elementIsNotRenderedWithId("vehicleRegistrationNumberLabel")
        elementIsNotRenderedWithId("vehicleRegistrationNumber")

      case Yes =>
        textOfElementWithId("vehicleRegistrationNumberLabel") mustBe "Vehicle registration number"
        textOfElementWithId("vehicleRegistrationNumber") mustBe declaration.maybeRegistrationNumber.get
    }
  }

  def assertClickOnPayButtonRedirectsToPayFrontend(): Unit = {
    val button = find(NameQuery("payButton")).get
    click on button

    // to do find a better assertion
    val redirectedTo = readPath()
    val successfulRedirectDependingOnEnvironment =
      redirectedTo == "/pay/card-billing-address" || redirectedTo == "/merchandise-in-baggage/process-payment"
    successfulRedirectDependingOnEnvironment mustBe true
  }
}


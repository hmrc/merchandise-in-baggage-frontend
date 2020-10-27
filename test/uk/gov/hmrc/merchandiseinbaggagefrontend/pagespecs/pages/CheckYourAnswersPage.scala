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

import com.github.tomakehurst.wiremock.WireMockServer
import org.openqa.selenium.WebDriver
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{AmountInPence, Declaration, JourneyInSmallVehicle}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.CheckYourAnswersPage.expectedSectionHeaders
import collection.JavaConverters._
import uk.gov.hmrc.http.HeaderNames._

class CheckYourAnswersPage(implicit webDriver: WebDriver) extends PageWithCTA {

  import WebBrowser._

  def mustRenderDetail(declaration: Declaration, totalTaxDue: AmountInPence): Unit = patiently {
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
      textOfElementWithId("customsAgentAddress").contains(customsAgent.address.postcode.get) mustBe true
    }

    textOfElementWithId("eoriLabel") mustBe "EORI number"
    textOfElementWithId("eori") mustBe declaration.eori.toString

    textOfElementWithId("nameOfPersonCarryingTheGoodsLabel") mustBe "Name of person carrying goods"
    textOfElementWithId("nameOfPersonCarryingTheGoods") mustBe declaration.nameOfPersonCarryingTheGoods.toString

    textOfElementWithId("placeOfArrivalLabel") mustBe "Place of arrival"
    textOfElementWithId("placeOfArrival") mustBe declaration.journeyDetails.placeOfArrival.display

    textOfElementWithId("dateOfArrivalLabel") mustBe "Date of arrival"
    textOfElementWithId("dateOfArrival") mustBe declaration.journeyDetails.formattedDateOfArrival

    textOfElementWithId("travellingByVehicleLabel") mustBe "Travelling by vehicle"
    textOfElementWithId("travellingByVehicle") mustBe declaration.journeyDetails.travellingByVehicle.entryName

    declaration.journeyDetails match {
      case journeyInSmallVehicle: JourneyInSmallVehicle =>
        textOfElementWithId("vehicleRegistrationNumberLabel") mustBe "Vehicle registration number"
        textOfElementWithId("vehicleRegistrationNumber") mustBe journeyInSmallVehicle.registrationNumber
      case _ =>
        elementIsNotRenderedWithId("vehicleRegistrationNumberLabel")
        elementIsNotRenderedWithId("vehicleRegistrationNumber")
    }
  }

  def mustRedirectToPaymentFromTheCTA(): Unit = {
    val button = find(NameQuery("payButton")).get
    click on button

    readPath() mustBe "/pay/initiate-journey"
  }

  def mustHaveOneRequestAndSessionId(server: WireMockServer): Assertion = {
    val payApiRequestCapture = server.getAllServeEvents.asScala
      .find(_.getRequest.getAbsoluteUrl.contains("pay-api/mib-frontend/mib/journey/start"))
      .get.getRequest

    payApiRequestCapture.header(xSessionId).values.size mustBe 1
    payApiRequestCapture.header(xRequestId).values.size mustBe 1
  }

  def mustRedirectToInvalidRequest(): Assertion =
    readPath() mustBe "/merchandise-in-baggage/invalid-request"
}

object CheckYourAnswersPage {
  val path = "/merchandise-in-baggage/check-your-answers"
  val title = "Check your answers before making your declaration"

  val expectedSectionHeaders =
    Seq("Details of the goods", "Personal details", "Journey details", "Now send your declaration")
}
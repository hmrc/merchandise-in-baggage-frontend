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

package uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs

import com.github.tomakehurst.wiremock.WireMockServer
import com.softwaremill.macwire.wire
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.http.HeaderNames.{xRequestId, xSessionId}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{AmountInPence, Declaration, DeclarationType, JourneyInSmallVehicle}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.CheckYourAnswersPage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CheckYourAnswersPage, InvalidRequestPage}
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.PayApiStub._

import scala.collection.JavaConverters._

class CheckYourAnswersPageSpec extends BasePageSpec[CheckYourAnswersPage] with TaxCalculation{
  override lazy val page: CheckYourAnswersPage = wire[CheckYourAnswersPage]

  "the page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWithNoBackButton(path)

    "render correctly" when {
      "the declaration is complete" in {
        val taxDue = givenADeclarationWithTaxDue(completedDeclarationJourney).futureValue
        val declaration = completedDeclarationJourney.declarationIfRequiredAndComplete.get

        open(path)

        page.headerText() mustBe title
        mustRenderDetail(declaration, taxDue.totalTaxDue)
      }

      "the declaration is complete but sparse" in {
        val declaration = sparseCompleteDeclarationJourney.declarationIfRequiredAndComplete.get
        val taxDue = givenADeclarationWithTaxDue(sparseCompleteDeclarationJourney).futureValue

        open(path)

        mustRenderDetail(declaration, taxDue.totalTaxDue)
      }
    }

    "redirect to /goods-over-threshold" when {
      "the total GBP value of the goods exceeds the threshold" in {
        givenADeclarationWithTaxDue(completedImportJourneyWithGoodsOverThreshold)

        open(path) mustBe "/merchandise-in-baggage/goods-over-threshold"
      }
    }

    s"redirect to ${InvalidRequestPage.path}" when {
      "the declaration journey is not complete" in {
        givenADeclarationJourney(incompleteDeclarationJourney)

        open(path) mustBe InvalidRequestPage.path
      }
    }

    "allow the user to make a payment" in {
      givenADeclarationWithTaxDue(completedDeclarationJourney).futureValue
      givenTaxArePaid(wireMockServer)

      open(path)

      page.mustRedirectToPaymentFromTheCTA()
      mustHaveOneRequestAndSessionId(wireMockServer)
    }

    "allow the user to make a declaration if exporting" in {
      givenADeclarationWithTaxDue(completedDeclarationJourney.copy(declarationType = DeclarationType.Export)).futureValue
      givenTaxArePaid(wireMockServer)

      open(path)

      page.mustRedirectToDeclarationConfirmation()
    }
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

  import WebBrowser._
  import page._

  def mustRenderDetail(declaration: Declaration, totalTaxDue: AmountInPence): Unit = patiently {
    findAll(TagNameQuery("h2")).map(_.underlying.getText).toSeq.dropRight(1) mustBe expectedSectionHeaders

    def textOfElementWithId(id: String): String = find(IdQuery(id)).get.underlying.getText

    def elementIsNotRenderedWithId(id: String): Assertion = find(IdQuery(id)).isEmpty mustBe true

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
}

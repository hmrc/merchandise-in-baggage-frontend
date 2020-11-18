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

import java.time.LocalDateTime

import com.github.tomakehurst.wiremock.WireMockServer
import com.softwaremill.macwire.wire
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.http.HeaderNames.{xRequestId, xSessionId}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{AmountInPence, Declaration, DeclarationId, DeclarationJourney, DeclarationType, JourneyInSmallVehicle, SessionId}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.CheckYourAnswersPage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages._
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.PayApiStub._

import scala.collection.JavaConverters._

class CheckYourAnswersPageSpec extends BasePageSpec[CheckYourAnswersPage] with TaxCalculation {
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

    Seq(1, 2).foreach { index =>
      s"redirect to ${GoodsTypeQuantityPage.path(index)}" when {
        "the user clicks on the change type link" in {
          givenADeclarationJourney(completedDeclarationJourney)
          open(path)
          page.clickOnChangeGoodsTypeLink(index-1) mustBe GoodsTypeQuantityPage.path(index)
        }

        "the user clicks on the change quantity link" in {
          givenADeclarationJourney(completedDeclarationJourney)
          open(path)
          page.clickOnChangeGoodsQuantityLink(index-1) mustBe GoodsTypeQuantityPage.path(index)
        }
      }

      s"redirect to ${GoodsVatRatePage.path(index)}" when {
        "the user clicks on the change link" in {
          givenADeclarationJourney(completedDeclarationJourney)
          open(path)
          page.clickOnChangeVatRateLink(index - 1) mustBe GoodsVatRatePage.path(index)
        }
      }

      s"redirect to ${SearchGoodsCountryPage.path(index)}" when {
        "the user clicks on the change link" in {
          givenADeclarationJourney(completedDeclarationJourney)
          open(path)
          page.clickOnChangeGoodsCountryLink(index - 1) mustBe SearchGoodsCountryPage.path(index)
        }
      }

      s"redirect to ${PurchaseDetailsPage.path(index)}" when {
        "the user clicks on the change link" in {
          givenADeclarationJourney(completedDeclarationJourney)
          open(path)
          page.clickOnChangePurchaseDetailsLink(index - 1) mustBe PurchaseDetailsPage.path(index)
        }
      }

      s"redirect to ${RemoveGoodsPage.path(index)}" when {
        "the user clicks on the remove link" in {
          givenADeclarationJourney(completedDeclarationJourney)
          open(path)
          page.clickOnRemoveGoodsLink(index - 1) mustBe RemoveGoodsPage.path(index)
        }
      }
    }

    s"redirect to ${AgentDetailsPage.path}" when {
      "the user clicks on the change link" in {
        givenADeclarationJourney(completedDeclarationJourney)
        open(path)
        page.clickOnChangeCustomsAgentLink() mustBe AgentDetailsPage.path
      }
    }

    s"redirect to ${EoriNumberPage.path}" when {
      "the user clicks on the change link" in {
        givenADeclarationJourney(completedDeclarationJourney)
        open(path)
        page.clickOnChangeEoriLink() mustBe EoriNumberPage.path
      }
    }

    s"redirect to ${TravellerDetailsPage.path}" when {
      "the user clicks on the change link" in {
        givenADeclarationJourney(completedDeclarationJourney)
        open(path)
        page.clickOnChangeTravellerDetailsLink() mustBe TravellerDetailsPage.path
      }
    }

    s"redirect to ${EnterEmailPage.path}" when {
      "the user clicks on the change link" in {
        givenADeclarationJourney(completedDeclarationJourney)
        open(path)
        page.clickOnChangeEmailAddressLink() mustBe EnterEmailPage.path
      }
    }

    s"redirect to ${JourneyDetailsPage.path}" when {
      "the user clicks on the change place of arrival link" in {
        givenADeclarationJourney(completedDeclarationJourney)
        open(path)
        page.clickOnPlaceOfArrivalLink() mustBe JourneyDetailsPage.path
      }

      "the user clicks on the change date of arrival link" in {
        givenADeclarationJourney(completedDeclarationJourney)
        open(path)
        page.clickOnDateOfArrivalLink() mustBe JourneyDetailsPage.path
      }
    }

    s"redirect to ${GoodsInVehiclePage.path}" when {
      "the user clicks on the change link" in {
        givenADeclarationJourney(completedDeclarationJourney)
        open(path)
        page.clickOnTravellingByVehicleChangeLink() mustBe GoodsInVehiclePage.path
      }
    }

    s"redirect to ${VehicleRegistrationNumberPage.path}" when {
      "the user clicks on the change link" in {
        givenADeclarationJourney(completedDeclarationJourney)
        open(path)
        page.clickOnVehicleRegistrationNumberChangeLink() mustBe VehicleRegistrationNumberPage.path
      }
    }

    s"redirect to ${GoodsTypeQuantityPage.path(3)}" when {
      "the user clicks on add more goods link" in {
        givenADeclarationJourney(completedDeclarationJourney)
        open(path)
        page.clickOnAddMoreGoodsLink() mustBe GoodsTypeQuantityPage.path(3)
      }
    }

    "allow the user to make a payment" in {
      givenADeclarationWithTaxDue(completedDeclarationJourney).futureValue
      givenTaxArePaid(wireMockServer)
      givenDeclarationIsPersistedInBackend(wireMockServer)

      open(path)

      page.clickOnAcceptAndPayButton() mustBe "/pay/initiate-journey"
      mustHaveOneRequestAndSessionId(wireMockServer)
    }

    "allow the user to make a declaration if exporting" in {
      val sessionId = SessionId()
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val id = DeclarationId("1234")
      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = DeclarationType.Export,
          createdAt = created, declarationId = Some(id))

      givenDeclarationIsPersistedInBackend(wireMockServer)
      givenADeclarationWithTaxDue(exportJourney).futureValue
      givenTaxArePaid(wireMockServer)
      givenPersistedDeclarationIsFound(wireMockServer, exportJourney.declarationIfRequiredAndComplete.get, exportJourney.declarationId.get)

      open(path)

      page.clickOnMakeDeclarationButton() mustBe DeclarationConfirmationPage.path
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

      textOfElementWithId(s"vatRateLabel_$index") mustBe "VAT rate"
      textOfElementWithId(s"vatRate_$index") mustBe s"${goods.goodsVatRate.value}%"

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

    textOfElementWithId("emailAddressLabel") mustBe "Email address"
    textOfElementWithId("emailAddress") mustBe declaration.email.email

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

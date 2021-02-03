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

import com.github.tomakehurst.wiremock.WireMockServer
import com.softwaremill.macwire.wire
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser
import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderNames.{xRequestId, xSessionId}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, JourneyInSmallVehicle}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.CheckYourAnswersPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages._
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.stubs.PayApiStub._
import uk.gov.hmrc.merchandiseinbaggage.utils.DateUtils._

import scala.collection.JavaConverters._
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

class CheckYourAnswersPageSpec extends BasePageSpec[CheckYourAnswersPage] with TaxCalculation {
  override lazy val page: CheckYourAnswersPage = wire[CheckYourAnswersPage]

  "the page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWithNoBackButton(path)

    "render correctly" when {
      "the import declaration is complete" in {
        val taxDue = givenADeclarationWithTaxDue(completedDeclarationJourney).futureValue
        val declaration = completedDeclarationJourney.declarationIfRequiredAndComplete.get

        open(path)

        page.headerText() mustBe title
        mustRenderDetail(declaration, Some(taxDue.totalTaxDue))
      }

      "the export declaration is complete" in {
        val exportJourney = completedDeclarationJourney.copy(declarationType = Export)
        givenADeclarationWithTaxDue(exportJourney).futureValue

        open(path)

        page.headerText() mustBe title
        mustRenderDetail(exportJourney.declarationIfRequiredAndComplete.get)
      }

      "the declaration is complete but sparse" in {
        val declaration = sparseCompleteDeclarationJourney.declarationIfRequiredAndComplete.get
        val taxDue = givenADeclarationWithTaxDue(sparseCompleteDeclarationJourney).futureValue

        open(path)

        mustRenderDetail(declaration, Some(taxDue.totalTaxDue))
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
          page.clickOnChangeGoodsTypeLink(index) mustBe GoodsTypeQuantityPage.path(index)
        }

        "the user clicks on the change quantity link" in {
          givenADeclarationJourney(completedDeclarationJourney)
          open(path)
          page.clickOnChangeGoodsQuantityLink(index) mustBe GoodsTypeQuantityPage.path(index)
        }
      }

      s"redirect to ${GoodsVatRatePage.path(index)}" when {
        "the user clicks on the change link" in {
          givenADeclarationJourney(completedDeclarationJourney)
          open(path)
          page.clickOnChangeVatRateLink(index) mustBe GoodsVatRatePage.path(index)
        }
      }

      s"redirect to ${SearchGoodsCountryPage.path(index)}" when {
        "the user clicks on the change link" in {
          givenADeclarationJourney(completedDeclarationJourney)
          open(path)
          page.clickOnChangeGoodsCountryLink(index) mustBe SearchGoodsCountryPage.path(index)
        }
      }

      s"redirect to ${PurchaseDetailsPage.path(index)}" when {
        "the user clicks on the change link" in {
          givenADeclarationJourney(completedDeclarationJourney)
          open(path)
          page.clickOnChangePurchaseDetailsLink(index) mustBe PurchaseDetailsPage.path(index)
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
      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = DeclarationType.Export, createdAt = created, declarationId = stubbedDeclarationId)

      givenDeclarationIsPersistedInBackend(wireMockServer)
      givenADeclarationWithTaxDue(exportJourney).futureValue
      givenTaxArePaid(wireMockServer)
      givenPersistedDeclarationIsFound(exportJourney.declarationIfRequiredAndComplete.get, exportJourney.declarationId)

      open(path)

      page.clickOnMakeDeclarationButton() mustBe DeclarationConfirmationPage.path
    }
  }

  def mustHaveOneRequestAndSessionId(server: WireMockServer): Assertion = {
    val payApiRequestCapture = server.getAllServeEvents.asScala
      .find(_.getRequest.getAbsoluteUrl.contains("pay-api/mib-frontend/mib/journey/start"))
      .get
      .getRequest

    payApiRequestCapture.header(xSessionId).values.size mustBe 1
    payApiRequestCapture.header(xRequestId).values.size mustBe 1
  }

  def mustRedirectToInvalidRequest(): Assertion =
    readPath() mustBe "/declare-commercial-goods/cannot-access-page"

  import WebBrowser._
  import page._

  def mustRenderDetail(declaration: Declaration, totalTaxDue: Option[AmountInPence] = None)(implicit messages: Messages): Unit = patiently {
    findAll(TagNameQuery("h2")).map(_.underlying.getText).toSeq.dropRight(1) mustBe expectedSectionHeaders

    def textOfElementWithId(id: String): String = find(IdQuery(id)).get.underlying.getText

    def elementIsNotRenderedWithId(id: String): Assertion = find(IdQuery(id)).isEmpty mustBe true

    totalTaxDue.map(_ => textOfElementWithId("taxDueLabel") mustBe "Payment due")
    totalTaxDue.map(amount => textOfElementWithId("taxDueValue") mustBe amount.formattedInPounds)

    declaration.maybeCustomsAgent.fold {
      textOfElementWithId("customsAgentYesNoLabel") mustBe "Customs agent"
      textOfElementWithId("customsAgentYesNo") mustBe "No"
      elementIsNotRenderedWithId("customsAgentNameLabel")
      elementIsNotRenderedWithId("customsAgentName")

      elementIsNotRenderedWithId("customsAgentAddressLabel")
      elementIsNotRenderedWithId("customsAgentAddress")
    } { customsAgent =>
      textOfElementWithId("customsAgentYesNoLabel") mustBe "Customs agent"
      textOfElementWithId("customsAgentYesNo") mustBe "Yes"
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
    textOfElementWithId("emailAddress") mustBe declaration.email.get.email

    textOfElementWithId("placeOfArrivalLabel") mustBe {
      if (declaration.declarationType == Import) "Place of arrival" else "Place of Departure"
    }
    textOfElementWithId("placeOfArrival") mustBe messages(declaration.journeyDetails.port.displayName)

    textOfElementWithId("dateOfArrivalLabel") mustBe {
      if (declaration.declarationType == Import) "Date of arrival" else "Date of Departure"
    }
    textOfElementWithId("dateOfArrival") mustBe declaration.journeyDetails.dateOfTravel.formattedDate

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

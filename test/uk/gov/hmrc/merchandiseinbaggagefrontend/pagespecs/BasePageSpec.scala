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

import com.softwaremill.macwire.wire
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, GoodsDestination, GoodsVatRate, YesNo}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages._
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpecWithApplication, CoreTestData, WireMockSupport}

trait BasePageSpec[P <: BasePage] extends BaseSpecWithApplication with WireMockSupport with CoreTestData {
  implicit lazy val webDriver: HtmlUnitDriver = new HtmlUnitDriver(false)

  lazy val baseUrl: BaseUrl = BaseUrl(s"http://localhost:$port")

  def page: P

  def readPath(): String = new java.net.URL(webDriver.getCurrentUrl).getPath

  def open(path: String): String = {
    WebBrowser.goTo(s"${baseUrl.value}$path")
    readPath()
  }

  lazy val testOnlyDeclarationJourneyPage: TestOnlyDeclarationJourneyPage = wire[TestOnlyDeclarationJourneyPage]
  lazy val startImportPage: StartImportPage = wire[StartImportPage]
  lazy val startExportPage: StartExportPage = wire[StartExportPage]
  lazy val exciseAndRestrictedGoodsPage: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]
  lazy val goodsDestinationPage:   RadioButtonPage[GoodsDestination] = wire[RadioButtonPage[GoodsDestination]]
  lazy val goodsRouteDestinationPage: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]
  lazy val valueWeightOfGoodsPage: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]
  lazy val searchGoodsPage: GoodsTypeQuantityPage = wire[GoodsTypeQuantityPage]
  lazy val goodsVatRatePage: RadioButtonPage[GoodsVatRate] = wire[RadioButtonPage[GoodsVatRate]]
  lazy val searchGoodsCountryPage: SearchGoodsCountryPage = wire[SearchGoodsCountryPage]
  lazy val purchaseDetailsPage: PurchaseDetailsPage = wire[PurchaseDetailsPage]
  lazy val invoiceNumberPage: InvoiceNumberPage = wire[InvoiceNumberPage]
  lazy val reviewGoodsPage: ReviewGoodsPage = wire[ReviewGoodsPage]
  lazy val removeGoodsPage: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]
  lazy val goodsRemovedPage: GoodsRemovedPage = wire[GoodsRemovedPage]
  lazy val agentDetailsPage: AgentDetailsPage = wire[AgentDetailsPage]
  lazy val paymentCalculationPage: PaymentCalculationPage = wire[PaymentCalculationPage]
  lazy val eoriNumberPage: EoriNumberPage = wire[EoriNumberPage]
  lazy val journeyDetailsPage: JourneyDetailsPage = wire[JourneyDetailsPage]
  lazy val vehicleRegistrationNumberPage: VehicleRegistrationNumberPage = wire[VehicleRegistrationNumberPage]
  lazy val checkYourAnswersPage: CheckYourAnswersPage = wire[CheckYourAnswersPage]
  lazy val declarationConfirmationPage: DeclarationConfirmationPage = wire[DeclarationConfirmationPage]
  lazy val customsAgentPage: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]

  def givenAnImportJourneyIsStarted(): Unit = {
    open(StartImportPage.path)
    startImportPage.clickOnCTA()
  }

  def givenAnExportJourneyIsStarted(): Unit = {
    open(StartExportPage.path)
    startExportPage.clickOnCTA()
  }

  def givenADeclarationJourney(declarationJourney: DeclarationJourney): Unit = {
    open(TestOnlyDeclarationJourneyPage.path)
    testOnlyDeclarationJourneyPage.fillOutForm(declarationJourney)
    testOnlyDeclarationJourneyPage.clickOnCTA()
  }

  def givenACompleteDeclarationJourney(): Unit = givenADeclarationJourney(completedDeclarationJourney)

  def aPageWhichRenders(path: String, setUp: => Unit = Unit, expectedTitle: String): Unit =
    s"render basic content with path $path title '$expectedTitle''" in {
      setUp
      open(path)
      readPath() mustBe path
      page.headerText() mustBe expectedTitle
    }

  def aPageWhichRequiresADeclarationJourney(path: String): Unit = {
    s"redirect from $path to ${InvalidRequestPage.path}" when {
      "the declaration has not been started" in {
        open(path) mustBe InvalidRequestPage.path
      }
    }
  }

  def aPageWhichRequiresACustomsAgentDeclaration(path: String): Unit = {
    s"redirect to ${InvalidRequestPage.path}" when {
      "the declaration has been started but the user has not declared whether or not they are a customs agent" in {
        givenADeclarationJourney(completedDeclarationJourney.copy(maybeIsACustomsAgent = None))
        open(path) mustBe InvalidRequestPage.path
      }
    }
  }

  def aPageThatRequiresACompletedGoodsEntry(path: String): Unit = {
    s"redirect to ${InvalidRequestPage.path}" when {
      "there are no started goods entries" in {
        givenADeclarationJourney(startedImportJourney)
        open(path) mustBe InvalidRequestPage.path
      }

      "there is no complete goods entries" in {
        givenADeclarationJourney(importJourneyWithStartedGoodsEntry)
        open(path) mustBe InvalidRequestPage.path
      }

      "there are incomplete goods entries" in {
        givenADeclarationJourney(importJourneyWithOneCompleteAndOneStartedGoodsEntry)
        open(path) mustBe InvalidRequestPage.path
      }
    }
  }

  def givenAGoodsEntryIsComplete(): Unit = givenADeclarationJourney(importJourneyWithOneCompleteGoodsEntry)

  def givenTwoGoodsEntriesAreComplete(): Unit = givenADeclarationJourney(importJourneyWithTwoCompleteGoodsEntries)
}


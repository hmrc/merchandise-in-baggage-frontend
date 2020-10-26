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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages._
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpecWithApplication, CoreTestData, WireMockSupport}

trait BasePageSpec[P <: BasePage] extends BaseSpecWithApplication with WireMockSupport with CoreTestData {
  implicit lazy val webDriver: HtmlUnitDriver = new HtmlUnitDriver(false)

  lazy val baseUrl: BaseUrl = BaseUrl(s"http://localhost:$port")

  def page: P

  lazy val testOnlyDeclarationJourneyPage: TestOnlyDeclarationJourneyPage = wire[TestOnlyDeclarationJourneyPage]
  lazy val startImportPage: StartImportPage = wire[StartImportPage]
  lazy val exciseAndRestrictedGoodsPage: ExciseAndRestrictedGoodsPage = wire[ExciseAndRestrictedGoodsPage]
  lazy val goodsDestinationPage: GoodsDestinationPage = wire[GoodsDestinationPage]
  lazy val valueWeightOfGoodsPage: ValueWeightOfGoodsPage = wire[ValueWeightOfGoodsPage]
  lazy val searchGoodsPage: SearchGoodsPage = wire[SearchGoodsPage]
  lazy val goodsVatRatePage: GoodsVatRatePage = wire[GoodsVatRatePage]
  lazy val searchGoodsCountryPage: SearchGoodsCountryPage = wire[SearchGoodsCountryPage]
  lazy val purchaseDetailsPage: PurchaseDetailsPage = wire[PurchaseDetailsPage]
  lazy val invoiceNumberPage: InvoiceNumberPage = wire[InvoiceNumberPage]
  lazy val reviewGoodsPage: ReviewGoodsPage = wire[ReviewGoodsPage]
  lazy val agentDetailsPage: AgentDetailsPage = wire[AgentDetailsPage]
  lazy val eoriNumberPage: EoriNumberPage = wire[EoriNumberPage]
  lazy val journeyDetailsPage: JourneyDetailsPage = wire[JourneyDetailsPage]
  lazy val vehicleRegistrationNumberPage: VehicleRegistrationNumberPage = wire[VehicleRegistrationNumberPage]
  lazy val checkYourAnswersPage: CheckYourAnswersPage = wire[CheckYourAnswersPage]

  def givenAnImportJourneyIsStarted(): Unit = {
    startImportPage.open()
    startImportPage.clickOnCTA()
  }

  def givenADeclarationJourney(declarationJourney: DeclarationJourney): Unit = {
    testOnlyDeclarationJourneyPage.open()
    testOnlyDeclarationJourneyPage.fillOutForm(declarationJourney)
    testOnlyDeclarationJourneyPage.clickOnCTA()
  }

  def givenACompleteDeclarationJourney(): Unit = givenADeclarationJourney(completedDeclarationJourney)

  def aPageWhichRenders(setUp: => Unit = Unit, expectedTitle: String): Unit =
    s"render basic content with title '$expectedTitle''" in {
      setUp
      page.open()
      page.mustRenderBasicContent(expectedTitle)
    }

  def aPageWhichRequiresADeclarationJourney() : Unit = {
    s"redirect to ${InvalidRequestPage.path}" when {
      "the declaration has not been started" in {
        page.open() mustBe InvalidRequestPage.path
      }
    }
  }

  def aPageWhichRequiresACustomsAgentDeclaration() : Unit = {
    s"redirect to ${InvalidRequestPage.path}" when {
      "the declaration has been started but the user has not declared whether or not they are a customs agent" in {
        givenADeclarationJourney(completedDeclarationJourney.copy(maybeIsACustomsAgent = None))
        page.open() mustBe InvalidRequestPage.path
      }
    }
  }
}


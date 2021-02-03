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

import com.softwaremill.macwire.wire
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData, WireMockSupport}

trait BasePageSpec[P <: BasePage] extends BaseSpecWithApplication with WireMockSupport with CoreTestData {
  implicit lazy val webDriver: HtmlUnitDriver = new HtmlUnitDriver(false)

  lazy val baseUrl: BaseUrl = BaseUrl(s"http://localhost:$port")

  def page: P

  val messageApi: Map[String, String] = messagesApi.messages("default")

  def readPath(): String = new java.net.URL(webDriver.getCurrentUrl).getPath

  def open(path: String): String = {
    WebBrowser.goTo(s"${baseUrl.value}$path")
    readPath()
  }

  lazy val testOnlyDeclarationJourneyPage: TestOnlyDeclarationJourneyPage = wire[TestOnlyDeclarationJourneyPage]
  lazy val startImportPage: StartImportPage = wire[StartImportPage]
  lazy val startExportPage: StartExportPage = wire[StartExportPage]

  def givenAnImportJourneyIsStarted(): Unit = {
    open(StartImportPage.path)
    startImportPage.clickOnCTA()
  }

  def givenAnExportJourneyIsStarted(): Unit = {
    open(StartExportPage.path)
    startExportPage.clickOnCTA()
  }

  def givenAnImportToNorthernIrelandJourneyIsStarted(): Unit = givenADeclarationJourney(startedImportToNorthernIrelandJourney)

  def givenAnImportToGreatBritainJourneyIsStarted(): Unit = givenADeclarationJourney(startedImportToGreatBritainJourney)

  def givenADeclarationJourney(declarationJourney: DeclarationJourney): Unit = {
    open(TestOnlyDeclarationJourneyPage.path)
    testOnlyDeclarationJourneyPage.fillOutForm(declarationJourney)
    testOnlyDeclarationJourneyPage.clickOnCTA()
  }

  def givenASecondGoodsEntryIsStarted(): Unit =
    givenADeclarationJourney(importJourneyWithOneCompleteAndOneStartedGoodsEntry)

  def givenACompleteDeclarationJourney(): Unit = givenADeclarationJourney(completedDeclarationJourney)

  def aPageWhichRenders(path: String, setUp: => Unit = Unit, expectedTitle: String): Unit =
    s"render basic content with path $path title '$expectedTitle'" in {
      setUp
      open(path)
      readPath() mustBe path
      page.headerText() mustBe expectedTitle
      page.bannerText() mustBe "beta"

      val contactLink = page.contactLink()
      contactLink.attribute("href").head.contains("/contact/problem_reports_nonjs?newTab=true&service=mib") mustBe true
      contactLink.underlying.getText mustBe "Is this page not working properly? (opens in new tab)"
    }

  def aPageWhichRequiresADeclarationJourney(path: String): Unit =
    s"redirect from $path to ${CannotAccessPage.path}" when {
      "the declaration has not been started" in {
        open(path) mustBe CannotAccessPage.path
      }
    }

  def aPageWhichRequiresACustomsAgentDeclaration(path: String): Unit =
    s"redirect to ${CannotAccessPage.path}" when {
      "the declaration has been started but the user has not declared whether or not they are a customs agent" in {
        givenADeclarationJourney(completedDeclarationJourney.copy(maybeIsACustomsAgent = None))
        open(path) mustBe CannotAccessPage.path
      }
    }

  def aPageThatRequiresAtLeastOneCompletedGoodsEntry(path: String): Unit =
    s"redirect to ${CannotAccessPage.path}" when {
      "there are no started goods entries" in {
        givenADeclarationJourney(startedImportJourney)
        open(path) mustBe CannotAccessPage.path
      }

      "there is no complete goods entries" in {
        givenADeclarationJourney(importJourneyWithStartedGoodsEntry)
        open(path) mustBe CannotAccessPage.path
      }
    }

  def aPageWithABackButton(path: String, setUp: => Unit = Unit, backPath: String, shouldGoCya: Boolean = true): Unit = {
    s"enable the user to navigate from $path back to $backPath" when {
      "the journey is incomplete" in {
        setUp
        open(path)
        page.maybeBackButton.isDefined mustBe true
        page.clickOnBackButton() mustBe backPath
      }
    }

    if (shouldGoCya) {
      s"enable the user to navigate back from $path to ${CheckYourAnswersPage.path} rather than $backPath" when {
        "the journey is complete" in {
          givenACompleteDeclarationJourney()
          open(path)
          page.maybeBackButton.isDefined mustBe true
          page.clickOnBackButton() mustBe CheckYourAnswersPage.path
        }
      }
    }
  }

  def aPageWithNoBackButton(path: String, setUp: => Unit = Unit): Unit =
    s"not display a back button" in {
      setUp
      open(path)
      page.maybeBackButton.isDefined mustBe false
    }

  def aPageWithARequiredQuestion(
    path: String,
    validationMessage: String,
    setUp: => Unit,
    validationMessageFieldId: String = "value-error"): Unit =
    s"display the validation message [$validationMessage] with id [$validationMessageFieldId]" when {
      "the user attempts to submit the form without answering the question" in {
        setUp
        open(path)
        page.clickOnCTA() mustBe path
        page.validationMessage(validationMessageFieldId) mustBe validationMessage
      }
    }

  def aPageWhichDisplaysValidationErrorMessagesInTheErrorSummary(path: String, validationMessages: Set[String], setUp: => Unit): Unit =
    s"display the currency validation message in the error summary list" when {
      "the user attempts to submit the form without answering the question" in {
        setUp
        open(path)
        page.clickOnCTA() mustBe path
        page.errorSummaryRows.toSet mustBe validationMessages
      }
    }

  def givenAGoodsEntryIsComplete(): Unit = givenADeclarationJourney(importJourneyWithOneCompleteGoodsEntry)

  def givenTwoGoodsEntriesAreComplete(): Unit = givenADeclarationJourney(importJourneyWithTwoCompleteGoodsEntries)
}

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

package uk.gov.hmrc.merchandiseinbaggage.smoketests

import org.openqa.selenium.{By, WebElement}
import org.scalatest.concurrent.Eventually
import org.scalatestplus.selenium.{HtmlUnit, WebBrowser}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, JourneyType}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.New
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.{Page, StartImportPage}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

class BaseUiSpec extends BaseSpecWithApplication with WireMockSupport with HtmlUnit with Eventually with CoreTestData {

  webDriver.setJavascriptEnabled(false)

  lazy val baseUrl = s"http://localhost:$port"

  def goto(path: String): Unit = WebBrowser.goTo(s"$baseUrl$path")

  def getCurrentUrl: String = webDriver.getCurrentUrl

  def fullUrl(path: String) = s"$baseUrl$path"

  private val findElement: By => WebElement = webDriver.findElement

  def findByXPath(xPath: String): WebElement = findElement(By.xpath(xPath))

  def findById(id: String): WebElement = findElement(By.id(id))

  def findByTagName(name: String): WebElement = findElement(By.tagName(name))

  def findByClassName(name: String): WebElement = findElement(By.className(name))

  def findByLinkText(name: String): WebElement = findElement(By.linkText(name))

  def elementText(element: WebElement): String = element.getText

  def submitPage[T](page: Page, formData: T): Unit =
    Try {
      page.submitPage(formData)
    } match {
      case Success(_) => ()
      case Failure(ex) =>
        println(s"\ncurrentPage: ${webDriver.getCurrentUrl}\n")
        ex.printStackTrace()
        fail()
    }

  //TODO smarter than before but we still could improve maybe by using a lib to capture/add session id
  def givenAJourneyWithSession(
    journeyType: JourneyType = New,
    declarationType: DeclarationType = Import,
    declarationJourney: DeclarationJourney = completedDeclarationJourney): DeclarationJourney = {
    goto(StartImportPage.path)

    (for {
      persisted <- declarationJourneyRepository.findAll()
      updated <- declarationJourneyRepository.upsert(
                  declarationJourney
                    .copy(sessionId = persisted.head.sessionId, journeyType = journeyType, declarationType = declarationType))
    } yield updated).futureValue
  }
}

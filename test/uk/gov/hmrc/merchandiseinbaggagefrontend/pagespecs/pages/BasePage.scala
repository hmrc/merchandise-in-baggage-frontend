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

import java.util

import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AppendedClues, Assertion}
import org.scalatestplus.selenium.WebBrowser

import scala.collection.JavaConverters._

final case class BaseUrl(value: String)

abstract class BasePage(implicit webDriver: WebDriver)
  extends Matchers with Eventually with IntegrationPatience with AppendedClues {

  import WebBrowser._

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(Span(2, Seconds)),
    interval = scaled(Span(150, Millis))
  )

  def mustRenderBasicContent(path: String, expectedTitle: String): Unit = patiently {
    val expectedHeadingContent: String = expectedTitle
    mustRenderBasicContentWithoutHeader(path, expectedTitle)
    headerText() mustBe expectedHeadingContent
  }

  def mustRenderBasicContentWithoutHeader(path: String, expectedTitle: String): Unit = patiently {
    readPath() mustBe path
    pageTitle mustBe expectedTitle
  }

  def headerText(): String = find(TagNameQuery("h1")).head.underlying.getText

  def bannerText(): String = find(ClassNameQuery("govuk-phase-banner__content__tag")).head.underlying.getText

  def contactLink(): Element = find(IdQuery("contactLink")).head

  def readPath(): String = new java.net.URL(webDriver.getCurrentUrl).getPath

  val element: String => WebElement = elementId => id(elementId).element.underlying

  def unifiedListItemsById(id: String): util.List[WebElement] =
    find(IdQuery(id)).get.underlying.findElements(By.tagName("li"))

  def textOfElementWithId(id: String): String = element(id).getText

  def attrOfElementWithId(id: String, attr: String): String = element(id).getAttribute(attr)

  def elementIsNotRenderedWithId(id: String): Assertion = find(IdQuery(id)).isEmpty mustBe true

  def patiently[A](assertionsMayTimeOut: => A): A = eventually(assertionsMayTimeOut).withClue {
    s"""
       |>>>page text was:
       |${webDriver.findElement(By.tagName("body")).getText}
       |>>>url was: ${webDriver.getCurrentUrl}
       |""".stripMargin
  }

  def maybeBackButton: Option[WebElement] =
    webDriver.findElements(By.className("govuk-back-link")).asScala.headOption

  def clickOnBackButton(): String = {
    val backButton = maybeBackButton.get
    click on backButton

    readPath()
  }
}



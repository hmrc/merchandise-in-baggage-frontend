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

import org.openqa.selenium.{By, WebDriver}
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AppendedClues, Assertion}
import org.scalatestplus.selenium.WebBrowser

final case class BaseUrl(value: String)

abstract class BasePage(baseUrl: BaseUrl)(implicit webDriver: WebDriver)
  extends Matchers with Eventually with IntegrationPatience with AppendedClues {

  import WebBrowser._

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout  = scaled(Span(2, Seconds)),
    interval = scaled(Span(150, Millis))
  )

  val path: String
  val expectedTitle: String
  val ctaName: String  = "continue"

  def mustRenderBasicContent(expectedTitle: String = expectedTitle): Unit = patiently{
    val expectedHeadingContent: String = expectedTitle
    readPath() mustBe path
    headerText() mustBe expectedHeadingContent
    pageTitle mustBe expectedTitle
  }

  def open(): Unit = WebBrowser.goTo(s"${baseUrl.value}$path")

  def headerText(): String = find(TagNameQuery("h1")).head.underlying.getText

  def readPath(): String = new java.net.URL(webDriver.getCurrentUrl).getPath

  def textOfElementWithId(id: String): String = find(IdQuery(id)).get.underlying.getText

  def elementIsNotRenderedWithId(id: String): Assertion = find(IdQuery(id)).isEmpty mustBe true

  def redirectsToInvalidRequest(): Assertion = patiently(readPath mustBe "/merchandise-in-baggage/invalid-request")

  def clickOnCTA(): String = {
    val button = find(NameQuery(ctaName)).get
    click on button

    readPath()
  }

  def patiently[A](assertionsMayTimeOut: => A): A = eventually(assertionsMayTimeOut).withClue {
    s"""
       |>>>page text was:
       |${webDriver.findElement(By.tagName("body")).getText}
       |>>>url was: ${webDriver.getCurrentUrl}
       |""".stripMargin
  }
}



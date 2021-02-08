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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages

import java.time.LocalDate

import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.support.ui.Select
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser
import org.scalatestplus.selenium.WebBrowser.{find, _}
import uk.gov.hmrc.merchandiseinbaggage.forms.JourneyDetailsForm._
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyDetailsEntry

class JourneyDetailsPage(implicit webDriver: WebDriver) extends DeclarationDataCapturePage[JourneyDetailsEntry] {

  import WebBrowser._

  def selectPort: Select = new Select(find(IdQuery(port)).get.underlying)

  def dayInput: Element = find(NameQuery(s"$dateOfTravel.day")).get

  def monthInput: Element = find(NameQuery(s"$dateOfTravel.month")).get

  def yearInput: Element = find(NameQuery(s"$dateOfTravel.year")).get

  override def fillOutForm(journeyDetailsEntry: JourneyDetailsEntry): Unit = {
    selectPort.selectByValue(journeyDetailsEntry.portCode)

    dayInput.underlying.clear()
    dayInput.underlying.sendKeys(journeyDetailsEntry.dateOfTravel.getDayOfMonth.toString)

    monthInput.underlying.clear()
    monthInput.underlying.sendKeys(journeyDetailsEntry.dateOfTravel.getMonthValue.toString)

    yearInput.underlying.clear()
    yearInput.underlying.sendKeys(journeyDetailsEntry.dateOfTravel.getYear.toString)
  }

  override def previouslyEnteredValuesAreDisplayed(journeyDetailsEntry: JourneyDetailsEntry): Assertion = {
    val selectedOptions = selectPort.getAllSelectedOptions
    selectedOptions.size() mustBe 1
    selectedOptions.listIterator().next().getAttribute("value") mustBe journeyDetailsEntry.portCode

    def valueMustEqual(element: Element, datePortion: Int) =
      element.underlying.getAttribute("value") mustBe datePortion.toString

    valueMustEqual(dayInput, journeyDetailsEntry.dateOfTravel.getDayOfMonth)
    valueMustEqual(monthInput, journeyDetailsEntry.dateOfTravel.getMonthValue)
    valueMustEqual(yearInput, journeyDetailsEntry.dateOfTravel.getYear)
  }

  def clickOnSubmitButtonMustRedirectTo(path: String): Assertion = patiently {
    val button = find(NameQuery("continue")).get
    click on button

    readPath() mustBe path
  }
}

object JourneyDetailsPage extends Page {
  val path = "/declare-commercial-goods/journey-details"

  val title = "Journey details"

  def submitPage()(implicit webDriver: HtmlUnitDriver): Unit = {
    val now = LocalDate.now()

    new Select(find(IdQuery(port)).get.underlying).selectByValue("DVR")

    find(NameQuery(s"$dateOfTravel.day")).get.underlying.sendKeys(now.getDayOfMonth.toString)
    find(NameQuery(s"$dateOfTravel.month")).get.underlying.sendKeys(now.getMonthValue.toString)
    find(NameQuery(s"$dateOfTravel.year")).get.underlying.sendKeys(now.getYear.toString)
    click.on(NameQuery("continue"))
  }
}

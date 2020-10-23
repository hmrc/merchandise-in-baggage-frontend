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

import java.time.LocalDate

import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.Select
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.JourneyDetailsForm._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.Port

class JourneyDetailsPage(baseUrl: BaseUrl)(implicit webDriver: WebDriver) extends PageWithCTA(baseUrl) {

  import WebBrowser._

  override val path = "/merchandise-in-baggage/journey-details"
  override val expectedTitle = "Journey details"

  def selectPlaceOfArrival: Select = new Select(find(IdQuery(placeOfArrival)).get.underlying)
  def dayInput: Element = find(NameQuery(s"$dateOfArrival.day")).get
  def monthInput: Element = find(NameQuery(s"$dateOfArrival.month")).get
  def yearInput: Element = find(NameQuery(s"$dateOfArrival.year")).get

  def fillOutForm(port: Port, date: LocalDate): Unit = {
    selectPlaceOfArrival.selectByValue(port.entryName)

    dayInput.underlying.clear()
    dayInput.underlying.sendKeys(date.getDayOfMonth.toString)

    monthInput.underlying.clear()
    monthInput.underlying.sendKeys(date.getMonthValue.toString)

    yearInput.underlying.clear()
    yearInput.underlying.sendKeys(date.getYear.toString)
  }

  def previouslyEnteredValuesAreDisplayed(port: Port, date: LocalDate): Unit = {
    val selectedOptions = selectPlaceOfArrival.getAllSelectedOptions
    selectedOptions.size() mustBe 1
    selectedOptions.listIterator().next().getText mustBe port.display

    def valueMustEqual(element: Element, datePortion: Int) =
      element.underlying.getAttribute("value") mustBe datePortion.toString

    valueMustEqual(dayInput, date.getDayOfMonth)
    valueMustEqual(monthInput, date.getMonthValue)
    valueMustEqual(yearInput, date.getYear)
  }

  def clickOnSubmitButtonMustRedirectTo(path: String): Assertion = patiently {
    val button = find(NameQuery("continue")).get
    click on button

    readPath() mustBe path
  }
}

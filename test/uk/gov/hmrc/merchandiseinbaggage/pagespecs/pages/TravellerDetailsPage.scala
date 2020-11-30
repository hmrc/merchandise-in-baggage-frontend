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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages

import org.openqa.selenium.WebDriver
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.merchandiseinbaggage.forms.TravellerDetailsForm
import uk.gov.hmrc.merchandiseinbaggage.model.core.Name

class TravellerDetailsPage(implicit webDriver: WebDriver) extends DeclarationDataCapturePage[Name]{

  import WebBrowser._

  def firstNameInput: Element = find(NameQuery(TravellerDetailsForm.firstName)).get
  def lastNameInput: Element = find(NameQuery(TravellerDetailsForm.lastName)).get

  override def fillOutForm(formData: Name): Unit = {
    def fill(input: Element, value: String): Unit = {
      input.underlying.clear()
      input.underlying.sendKeys(value)
    }

    fill(firstNameInput, formData.firstName)
    fill(lastNameInput, formData.lastName)
  }

  override def previouslyEnteredValuesAreDisplayed(formData: Name): Assertion = {
    def valueMustEqual(element: Element, value: String) =
      element.underlying.getAttribute("value") mustBe value

    valueMustEqual(firstNameInput, formData.firstName)
    valueMustEqual(lastNameInput, formData.lastName)
  }
}

object TravellerDetailsPage {
  val path: String = "/merchandise-in-baggage/traveller-details"
  val title: String = "What is the name of the person carrying the goods?"
}

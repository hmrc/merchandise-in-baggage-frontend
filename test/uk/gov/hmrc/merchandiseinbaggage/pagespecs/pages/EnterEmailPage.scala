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
import uk.gov.hmrc.merchandiseinbaggage.model.core.Email

class EnterEmailPage(implicit webDriver: WebDriver) extends DeclarationDataCapturePage[Email]{

  import WebBrowser._

  def emailInput: Element = find(NameQuery("email")).get
  def confirmationInput: Element = find(NameQuery("confirmation")).get

  override def fillOutForm(formData: Email): Unit = {
    def fill(input: Element, value: String): Unit = {
      input.underlying.clear()
      input.underlying.sendKeys(value)
    }

    fill(emailInput, formData.email)
    fill(confirmationInput, formData.confirmation)
  }

  override def previouslyEnteredValuesAreDisplayed(formData: Email): Assertion = {
    def valueMustEqual(element: Element, value: String) =
      element.underlying.getAttribute("value") mustBe value

    valueMustEqual(emailInput, formData.email)
    valueMustEqual(confirmationInput, formData.confirmation)
  }
}

object EnterEmailPage {
  val path: String = "/merchandise-in-baggage/enter-email"
  val title: String = "Enter an email address"
}


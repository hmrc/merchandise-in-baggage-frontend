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

import org.openqa.selenium.WebDriver
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser

class InvoiceNumberPage(implicit webDriver: WebDriver) extends DeclarationDataCapturePage[String] {

  import WebBrowser._

  def invoiceNumberInput: Element = find(NameQuery("value")).get

  override def fillOutForm(formData: String): Unit = {
    invoiceNumberInput.underlying.clear()
    invoiceNumberInput.underlying.sendKeys(formData)
  }

  override def previouslyEnteredValuesAreDisplayed(formData: String): Assertion = {
    invoiceNumberInput.underlying.getAttribute("value") mustEqual formData
  }
}

object InvoiceNumberPage {
  def path(idx: Int): String = s"/merchandise-in-baggage/invoice-number/$idx"

  val title = "What is the invoice number for the test good?"
}

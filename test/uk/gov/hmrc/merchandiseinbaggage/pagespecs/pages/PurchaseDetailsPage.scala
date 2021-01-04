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

import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.Select
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.merchandiseinbaggage.model.core.PurchaseDetailsInput

class PurchaseDetailsPage(implicit webDriver: WebDriver) extends DeclarationDataCapturePage[PurchaseDetailsInput] {

  import WebBrowser._

  def priceInput: Element = find(NameQuery("price")).get

  def selectCurrency: Select = new Select(find(IdQuery("currency")).get.underlying)

  override def fillOutForm(formData: PurchaseDetailsInput): Unit = {
    priceInput.underlying.clear()
    priceInput.underlying.sendKeys(formData.price)

    selectCurrency.selectByValue(formData.currency)
  }

  override def previouslyEnteredValuesAreDisplayed(formData: PurchaseDetailsInput): Assertion = {
    priceInput.underlying.getAttribute("value") mustEqual formData.price

    val selectedOptions = selectCurrency.getAllSelectedOptions
    selectedOptions.size() mustBe 1
    selectedOptions.listIterator().next().getAttribute("value") mustBe formData.currency
  }
}

object PurchaseDetailsPage {
  def path(idx: Int): String = s"/declare-commercial-goods/purchase-details/$idx"

  val title = "How much did you pay for the test good?"
}

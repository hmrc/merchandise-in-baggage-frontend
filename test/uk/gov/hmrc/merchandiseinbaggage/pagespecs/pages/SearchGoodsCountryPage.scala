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

class SearchGoodsCountryPage(implicit webDriver: WebDriver) extends DeclarationDataCapturePage[String] {

  import WebBrowser._

  def selectCountry: Select = new Select(find(IdQuery("country")).get.underlying)

  override def fillOutForm(formData: String): Unit =
    selectCountry.selectByValue(formData)

  override def previouslyEnteredValuesAreDisplayed(formData: String): Assertion = {
    val selectedOptions = selectCountry.getAllSelectedOptions
    selectedOptions.size() mustBe 1
    selectedOptions.listIterator().next().getAttribute("value") mustBe formData
  }

  override def validationMessage(className: String): String = {
    val errorListSummary = find(ClassNameQuery(className)).get
    errorListSummary.underlying.getText
  }
}

object SearchGoodsCountryPage {
  def path(idx: Int): String = s"/declare-commercial-goods/search-goods-country/$idx"

  val importTitle = "In what country did you buy the test good?"
  val exportTitle = "What country are you taking the test good to?"
  val importHint = "If you bought the goods on a plane or boat, enter the country you were travelling from at the time of purchase."
}

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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.CategoryQuantityOfGoods

class SearchGoodsPage(baseUrl: BaseUrl)(implicit webDriver: WebDriver) extends BasePage(baseUrl) {

  import WebBrowser._

  override val path = "/merchandise-in-baggage/search-goods/1"
  override val expectedTitle = "What type of goods are you bringing into the UK?"

  def categoryInput: Element = find(NameQuery("category")).get
  def quantityInput: Element = find(NameQuery("quantity")).get

  def fillOutForm(catQuan: CategoryQuantityOfGoods): Unit = {
    click on categoryInput
    enter(catQuan.category)

    click on quantityInput
    enter(catQuan.quantity)
  }

  def previouslyEnteredValuesAreDisplayed(catQuan: CategoryQuantityOfGoods): Unit = {
    categoryInput.underlying.getAttribute("value") mustBe catQuan.category
    quantityInput.underlying.getAttribute("value") mustBe catQuan.quantity
  }

  def clickOnSubmitButtonMustRedirectTo(path: String): Assertion = patiently {
    val button = find(NameQuery("continue")).get
    click on button

    readPath() mustBe path
  }
}

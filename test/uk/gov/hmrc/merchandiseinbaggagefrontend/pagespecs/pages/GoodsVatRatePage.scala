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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{CategoryQuantityOfGoods, GoodsVatRate}

class GoodsVatRatePage(baseUrl: BaseUrl)(implicit webDriver: WebDriver) extends BasePage(baseUrl) {

  import WebBrowser._

  override val path = "/merchandise-in-baggage/goods-vat-rate/1"
  override val expectedTitle = "Check which VAT rate applies to the"

  def selectOption(vatRate: GoodsVatRate): Unit = {
    click on find(IdQuery(vatRate.entryName)).get
  }

  def previouslySelectedOptionIsChecked(vatRate: GoodsVatRate): Unit = {
    find(IdQuery(vatRate.entryName)).get.underlying.getAttribute("checked") mustBe "true"
    find(IdQuery(vatRate.entryName)).get.isSelected mustBe true
  }

  def clickOnSubmitButtonMustRedirectTo(path: String): Assertion = patiently {
    val button = find(NameQuery("continue")).get
    click on button

    readPath() mustBe path
  }
}

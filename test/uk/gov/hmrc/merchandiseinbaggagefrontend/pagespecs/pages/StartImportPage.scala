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
import org.scalatestplus.selenium.WebBrowser

class StartImportPage(baseUrl: BaseUrl)(implicit webDriver: WebDriver) extends BasePage(baseUrl) {
  import WebBrowser._

  override val path = "/merchandise-in-baggage/start-import"
  override val expectedTitle = "Declaring goods in your baggage you're bringing to the UK to sell"
  override val expectedHeadingContent: String = expectedTitle

  def assertPageIsDisplayed(): Unit = patiently(ensureBasicContent())

  def clickOnStartNowButton(): Unit = {
    val button = find(NameQuery("startNow")).get
    click on button
  }
}


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
import play.api.libs.json.Json.{prettyPrint, toJson}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationJourney

class EoriNumberPage(baseUrl: BaseUrl)(implicit webDriver: WebDriver) extends BasePage(baseUrl) {

  import WebBrowser._

  override val path = "/merchandise-in-baggage/enter-eori-number"
  override val expectedTitle = "What is the EORI number of the company importing the goods?"

  def fillOutForm(eori: String): Unit = {
    click on find(IdQuery("eori")).get
    enter(eori)
  }

  def clickOnSubmitButton(): Assertion = {
    val button = find(NameQuery("continue")).get
    click on button

    readPath() mustBe "/merchandise-in-baggage/traveller-details"
  }
}

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

class VehicleRegistrationNumberPage(baseUrl: BaseUrl)(implicit webDriver: WebDriver) extends PageWithCTA(baseUrl) {
  override val path = "/merchandise-in-baggage/vehicle-registration-number"

  import WebBrowser._

  def fillOutForm(value: String): Unit = {
    click on find(NameQuery("value")).get
    enter(value)
  }

  def mustRedirectToCheckYourAnswersFromTheCTA(): Assertion = {
    click on find(NameQuery("continue")).get

    readPath() mustBe "/merchandise-in-baggage/check-your-answers"
  }
}

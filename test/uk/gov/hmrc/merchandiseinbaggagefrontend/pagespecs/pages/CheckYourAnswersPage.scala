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

class CheckYourAnswersPage(implicit webDriver: WebDriver) extends PageWithCTA {

  import WebBrowser._

  def mustRedirectToPaymentFromTheCTA(): Unit = {
    val button = find(NameQuery("payButton")).get
    click on button

    readPath() mustBe "/pay/initiate-journey"
  }

  def mustRedirectToDeclarationConfirmation(): Unit = {
    val button = find(NameQuery("makeDeclarationButton")).get
    click on button

    readPath() mustBe "/merchandise-in-baggage/declaration-confirmation"
  }

  def mustBeReidrecToExiceGoodAfterClicingNewDeclarationLink(): Unit = {
    val button = find(NameQuery("makeDeclarationButton")).get
    click on button

    readPath() mustBe "/merchandise-in-baggage/declaration-confirmation"
  }
}

object CheckYourAnswersPage {
  val path = "/merchandise-in-baggage/check-your-answers"
  val title = "Check your answers before making your declaration"

  val expectedSectionHeaders =
    Seq("Details of the goods", "Personal details", "Journey details", "Now send your declaration")
}
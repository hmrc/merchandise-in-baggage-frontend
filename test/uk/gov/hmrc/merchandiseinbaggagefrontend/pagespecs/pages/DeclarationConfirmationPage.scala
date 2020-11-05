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

import java.time.LocalDateTime

import org.openqa.selenium.WebDriver
import org.scalatestplus.selenium.WebBrowser._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.Declaration

class DeclarationConfirmationPage(implicit webDriver: WebDriver) extends BasePage {

  private val element: String => Element = elementId => id(elementId).element

  def hasConfirmationPanelWithContents(): Unit = {
    element("confirmationPanelId").attribute("class") mustBe Some("govuk-panel govuk-panel--confirmation")
    element("panelTitleId").text mustBe "Declaration complete"
    element("mibReferenceId").text must include("Your reference number")
    element("declarationDateId").text mustBe "Date of declaration"
    element("declarationDateFormattedId").text must include(LocalDateTime.now.format(Declaration.formatter))
  }
}

object DeclarationConfirmationPage {
  val path = "/merchandise-in-baggage/declaration-confirmation"
  val title = "Confirmation page"
}
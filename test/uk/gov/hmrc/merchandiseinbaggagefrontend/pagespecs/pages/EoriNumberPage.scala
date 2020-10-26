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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.Eori

class EoriNumberPage(implicit webDriver: WebDriver) extends DeclarationDataCapturePage[Eori] {

  import WebBrowser._

  private def input: Element = find(IdQuery("eori")).get

  override def previouslyEnteredValuesAreDisplayed(eori: Eori): Assertion =
    input.attribute("value") mustBe Some(eori.value)

  override def fillOutForm(eori: Eori): Unit = {
    click on input
    enter(eori.value)
  }
}

object EoriNumberPage {
  val path = "/merchandise-in-baggage/enter-eori-number"
  val expectedAgentTitle = "What is the EORI number of the company importing the goods?"
  val expectedNonAgentTitle = "What is your EORI number?"
}

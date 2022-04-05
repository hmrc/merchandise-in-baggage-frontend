/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.smoketests.pages

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.Assertions.fail
import org.scalatestplus.selenium.WebBrowser.{find, _}
import uk.gov.hmrc.merchandiseinbaggage.forms.TravellerDetailsForm
import uk.gov.hmrc.merchandiseinbaggage.model.api.Name

object TravellerDetailsPage extends Page {
  val path: String = "/declare-commercial-goods/traveller-details"
  val title: String = "What is the name of the person carrying the goods?"
  val hint: String = "Enter their full legal name as it appears on their passport or birth certificate."

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    val name = formData match {
      case n: Name => n
      case _       => fail("invalid_input")
    }

    find(NameQuery(TravellerDetailsForm.firstName)).get.underlying.sendKeys(name.firstName)
    find(NameQuery(TravellerDetailsForm.lastName)).get.underlying.sendKeys(name.lastName)
    click.on(NameQuery("continue"))
  }
}

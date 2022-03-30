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
import org.openqa.selenium.support.ui.Select
import org.scalatestplus.selenium.WebBrowser.{find, _}
import uk.gov.hmrc.merchandiseinbaggage.forms.JourneyDetailsForm._

import java.time.LocalDate

object JourneyDetailsPage extends Page {
  val path = "/declare-commercial-goods/journey-details"

  val title = "Journey details"

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    val now = LocalDate.now()

    new Select(find(IdQuery(port)).get.underlying).selectByValue(formData.toString)

    find(NameQuery(s"$dateOfTravel.day")).get.underlying.sendKeys(now.getDayOfMonth.toString)
    find(NameQuery(s"$dateOfTravel.month")).get.underlying.sendKeys(now.getMonthValue.toString)
    find(NameQuery(s"$dateOfTravel.year")).get.underlying.sendKeys(now.getYear.toString)
    click.on(NameQuery("continue"))
  }
}

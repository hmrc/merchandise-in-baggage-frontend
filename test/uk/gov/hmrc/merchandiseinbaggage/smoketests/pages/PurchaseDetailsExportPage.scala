/*
 * Copyright 2023 HM Revenue & Customs
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
import org.scalatestplus.selenium.WebBrowser._

object PurchaseDetailsExportPage extends Page {
  def path(idx: Int): String = s"/declare-commercial-goods/purchase-details/$idx"
  def title(idx: Int)        = "How much did you pay for the test good?"

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    find(NameQuery("price")).get.underlying.sendKeys(formData.toString)
    click.on(NameQuery("continue"))
  }

}

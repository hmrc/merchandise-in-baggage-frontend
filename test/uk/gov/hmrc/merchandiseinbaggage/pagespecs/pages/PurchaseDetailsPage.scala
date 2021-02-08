/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.support.ui.Select
import org.scalatestplus.selenium.WebBrowser.{find, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.PurchaseDetailsInput

object PurchaseDetailsPage extends Page {
  def path(idx: Int): String = s"/declare-commercial-goods/purchase-details/$idx"
  def title(idx: Int) = "How much did you pay for the test good?"

  def selectCurrency(implicit webDriver: HtmlUnitDriver): Select = new Select(find(IdQuery("currency")).get.underlying)

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    val pd = formData.asInstanceOf[PurchaseDetailsInput]
    find(NameQuery("price")).get.underlying.sendKeys(pd.price)
    selectCurrency.selectByValue(pd.currency)
    click.on(NameQuery("continue"))
  }

}

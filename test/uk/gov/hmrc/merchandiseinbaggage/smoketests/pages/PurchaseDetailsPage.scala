/*
 * Copyright 2024 HM Revenue & Customs
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
import org.scalatest.Assertions.fail
import org.scalatest.{Assertion, Suite}
import org.scalatestplus.selenium.WebBrowser._
import uk.gov.hmrc.merchandiseinbaggage.model.core.PurchaseDetailsInput
import uk.gov.hmrc.merchandiseinbaggage.smoketests.BaseUiSpec
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.PurchaseDetailsPage.path

object PurchaseDetailsPage extends Page {
  def path(idx: Int): String  = s"/declare-commercial-goods/purchase-details/$idx"
  def title(idx: Int): String = "How much did you pay for the test good?"

  def selectCurrency(implicit webDriver: HtmlUnitDriver): Select = new Select(find(IdQuery("currency")).get.underlying)

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    val pd = formData match {
      case pdi: PurchaseDetailsInput => pdi
      case _                         => fail("invalid_input")
    }

    find(NameQuery("price")).get.underlying.sendKeys(pd.price)
    selectCurrency.selectByValue(pd.currency)
    click.on(NameQuery("continue"))
  }
}

trait PurchaseDetailsPage extends BaseUiSpec { this: Suite =>

  def goToPurchaseDetailsPage(idx: Int): Assertion = {
    goto(path(idx))
    pageTitle                        must startWith(messages(s"purchaseDetails.title", "wine"))
    elementText(findByTagName("h1")) must startWith(messages(s"purchaseDetails.heading", "wine"))
  }
}

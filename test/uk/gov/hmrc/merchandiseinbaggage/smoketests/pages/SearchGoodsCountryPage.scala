/*
 * Copyright 2025 HM Revenue & Customs
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
import org.scalatestplus.selenium.WebBrowser._

object SearchGoodsCountryPage extends Page {
  def path(idx: Int): String = s"/declare-commercial-goods/search-goods-country/$idx"

  def importTitle(idx: Int): String = "In what country did you buy the test good?"
  def exportTitle(idx: Int): String = "What country are you taking the test good to?"
  val importHint                    =
    "If you bought the goods on a plane or boat, enter the country you were travelling from at the time of purchase."

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    new Select(find(IdQuery("country")).get.underlying).selectByValue(formData.toString)
    click.on(NameQuery("continue"))
  }
}

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

import enumeratum.EnumEntry
import org.openqa.selenium.WebDriver
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser

abstract class RadioButtonPage[E <: EnumEntry](baseUrl: BaseUrl)(implicit webDriver: WebDriver)
  extends DeclarationDataCapturePage[E](baseUrl: BaseUrl) {

  import WebBrowser._

  def radioButtonFor(enum: E): Element = find(IdQuery(enum.entryName)).get

  override def fillOutForm(enum: E): Unit = click on radioButtonFor(enum)

  override def previouslyEnteredValuesAreDisplayed(enum: E): Assertion =
    radioButtonFor(enum).underlying.getAttribute("checked") mustBe "true"
}

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
import org.scalatest.{Assertion, Suite}
import org.scalatestplus.selenium.WebBrowser.{find, _}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.BaseUiSpec
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.DeclarationNotFoundPage._

object DeclarationNotFoundPage extends Page {
  val path = "/declare-commercial-goods/declaration-not-found"
  val title =
    "Declaration not found - Declare commercial goods carried in accompanied baggage or small vehicles - GOV.UK"

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    val button = find(IdQuery(formData.toString)).get
    click on button
  }
}

trait DeclarationNotFoundPage extends BaseUiSpec { this: Suite =>

  def goToDeclarationNotFoundPage: Assertion = {
    goto(path)
    pageTitle mustBe title
  }
}

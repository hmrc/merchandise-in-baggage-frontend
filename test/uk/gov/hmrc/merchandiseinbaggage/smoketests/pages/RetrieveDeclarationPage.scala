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
import org.scalatest.Assertions.fail
import org.scalatestplus.selenium.WebBrowser.*
import uk.gov.hmrc.merchandiseinbaggage.model.core.RetrieveDeclaration

object RetrieveDeclarationPage extends Page {
  val path  = "/declare-commercial-goods/retrieve-declaration"
  val title =
    "What are the details of your existing declaration? - Declare commercial goods carried in accompanied baggage or small vehicles - GOV.UK"

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {

    val rd = formData match {
      case rd: RetrieveDeclaration => rd
      case _                       => fail("invalid_input")
    }

    find(NameQuery("mibReference")).get.underlying.sendKeys(rd.mibReference.value)
    find(NameQuery("eori")).get.underlying.sendKeys(rd.eori.value)
    click.on(NameQuery("continue"))
  }
}

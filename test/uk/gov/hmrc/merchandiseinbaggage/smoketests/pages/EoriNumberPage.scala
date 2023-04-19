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
import org.scalatest.{Assertion, Suite}
import org.scalatestplus.selenium.WebBrowser._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.BaseUiSpec
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.EoriNumberPage.{expectedAgentExportTitle, expectedAgentImportTitle, expectedNonAgentTitle, path}

object EoriNumberPage extends Page {
  val path = "/declare-commercial-goods/enter-eori-number"
  val expectedAgentImportTitle = "What is the EORI number of the business bringing the goods into Great Britain?"
  val expectedAgentExportTitle = "What is the EORI number of the business taking the goods out of Great Britain?"
  val expectedNonAgentTitle = "What is your EORI number?"

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    find(IdQuery("eori")).get.underlying.sendKeys(formData.toString)
    click.on(NameQuery("continue"))
  }
}

trait EoriNumberPage extends BaseUiSpec { this: Suite =>

  def goToEoriPage(customerAgent: YesNo, declarationType: DeclarationType = Import): Assertion = {
    goto(path)
    val titleStart = (customerAgent, declarationType) match {
      case (Yes, Import) => expectedAgentImportTitle
      case (Yes, Export) => expectedAgentExportTitle
      case (No, _)       => expectedNonAgentTitle
    }
    pageTitle must startWith(titleStart)
  }
}

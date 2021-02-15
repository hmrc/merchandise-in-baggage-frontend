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

package uk.gov.hmrc.merchandiseinbaggage.smoketests.pages

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.Assertions.fail
import org.scalatest.{Assertion, Suite}
import org.scalatestplus.selenium.WebBrowser._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.BaseUiSpec
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.CheckYourAnswersPage._

object CheckYourAnswersPage extends Page {
  val path = "/declare-commercial-goods/check-your-answers"
  val title =
    "Check your answers before making your declaration - Declare commercial goods carried in accompanied baggage or small vehicles - GOV.UK"

  val expectedSectionHeaders =
    Seq("Details of the goods", "Personal details", "Journey details", "Now send your declaration")

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    val query = formData match {
      case Export => "makeDeclarationButton"
      case Import => "payButton"
      case _      => fail("invalid_input")
    }

    val button = find(NameQuery(query)).get
    click on button
  }
}

trait CheckYourAnswersPage extends BaseUiSpec { this: Suite =>

  def goToCYAPage: Assertion = {
    goto(path)
    pageTitle mustBe title
  }
}

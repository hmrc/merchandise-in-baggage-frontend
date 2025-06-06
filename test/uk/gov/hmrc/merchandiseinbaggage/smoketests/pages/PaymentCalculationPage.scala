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
import org.scalatestplus.selenium.WebBrowser._
import uk.gov.hmrc.merchandiseinbaggage.model.api.AmountInPence
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

object PaymentCalculationPage extends Page {
  val path: String = "/declare-commercial-goods/payment-calculation"

  def title(amountInPence: AmountInPence): String = s"Payment due on these goods ${amountInPence.formattedInPounds}"

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    val button = find(ClassNameQuery("govuk-button")).get
    click on button
  }
}

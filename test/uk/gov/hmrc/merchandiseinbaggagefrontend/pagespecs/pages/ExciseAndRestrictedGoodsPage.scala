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

import org.openqa.selenium.WebDriver
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo

class ExciseAndRestrictedGoodsPage(baseUrl: BaseUrl)(implicit webDriver: WebDriver)
  extends DeclarationDataCapturePage[YesNo](baseUrl) {
  import WebBrowser._

  override val path: String = ExciseAndRestrictedGoodsPage.path

  override val expectedTitle = "Are you bringing in excise goods or restricted goods?"

  def radioButtonFor(yesNo: YesNo): Element = find(IdQuery(yesNo.entryName)).get

  override def previouslyEnteredValuesAreDisplayed(exciseAndRestrictedGoods: YesNo): Assertion =
    radioButtonFor(exciseAndRestrictedGoods).underlying.getAttribute("checked") mustBe "true"

  override def fillOutForm(exciseAndRestrictedGoods: YesNo): Unit = click on radioButtonFor(exciseAndRestrictedGoods)
}

object ExciseAndRestrictedGoodsPage{
  val path = "/merchandise-in-baggage/excise-and-restricted-goods"
}
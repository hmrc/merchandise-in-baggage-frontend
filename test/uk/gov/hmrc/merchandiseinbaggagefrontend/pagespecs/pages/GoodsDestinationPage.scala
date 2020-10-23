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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestination

class GoodsDestinationPage(baseUrl: BaseUrl)(implicit webDriver: WebDriver)
  extends DeclarationDataCapturePage[GoodsDestination](baseUrl) {
  import WebBrowser._

  override val path: String = GoodsDestinationPage.path

  override val expectedTitle = "Where in the UK are the goods going?"

  def radioButtonFor(goodsDestination: GoodsDestination): Element =
    find(IdQuery(goodsDestination.entryName)).get

  override def fillOutForm(goodsDestination: GoodsDestination): Unit =
    click on radioButtonFor(goodsDestination)

  override def previouslyEnteredValuesAreDisplayed(goodsDestination: GoodsDestination): Assertion =
    radioButtonFor(goodsDestination).underlying.getAttribute("checked") mustBe "true"
}

object GoodsDestinationPage {
  val path = "/merchandise-in-baggage/goods-destination"
}

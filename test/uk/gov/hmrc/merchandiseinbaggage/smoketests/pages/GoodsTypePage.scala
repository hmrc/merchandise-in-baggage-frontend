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
import org.scalatest.{Assertion, Suite}
import org.scalatestplus.selenium.WebBrowser._
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyType
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.New
import uk.gov.hmrc.merchandiseinbaggage.smoketests.BaseUiSpec
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.GoodsTypePage.path

object GoodsTypePage extends Page {
  def path(idx: Int): String = s"/declare-commercial-goods/goods-type/$idx"

  def submitPage[T](category: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    find(NameQuery("category")).get.underlying.sendKeys(category.toString)
    click.on(NameQuery("continue"))
  }
}

trait GoodsTypePage extends BaseUiSpec {
  this: Suite =>

  def title(idx: Int, journeyType: JourneyType): String =
    messages(s"goodsType.$journeyType.${if (idx == 1 && journeyType == New) "" else "next."}title")

  def goToGoodsTypePage(idx: Int, journeyType: JourneyType): Assertion = {
    goto(path(idx))
    pageTitle must startWith(title(idx, journeyType))
  }
}

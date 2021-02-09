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
import org.scalatestplus.selenium.WebBrowser._
import uk.gov.hmrc.merchandiseinbaggage.model.api.CategoryQuantityOfGoods

object GoodsTypeQuantityPage extends Page {
  def path(idx: Int): String = s"/declare-commercial-goods/goods-type-quantity/$idx"
  def title(idx: Int) = s"Enter the ${if (idx == 1) "first" else "next"} type of goods"

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    val cqg = formData match {
      case c: CategoryQuantityOfGoods => c
      case _                          => fail("invalid_input")
    }

    find(NameQuery("category")).get.underlying.sendKeys(cqg.category)
    find(NameQuery("quantity")).get.underlying.sendKeys(cqg.quantity)
    click.on(NameQuery("continue"))
  }
}

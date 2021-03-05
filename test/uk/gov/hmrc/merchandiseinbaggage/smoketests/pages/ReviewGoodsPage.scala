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
import org.scalatest.{Assertion, Suite}
import org.scalatestplus.selenium.WebBrowser._
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyType
import uk.gov.hmrc.merchandiseinbaggage.smoketests.BaseUiSpec
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.ReviewGoodsPage._

object ReviewGoodsPage extends Page {
  val path: String = "/declare-commercial-goods/review-goods"
  val title = "Review your goods - Declare commercial goods carried in accompanied baggage or small vehicles - GOV.UK"
  val addingDeclarationTitle =
    "Review the goods you are adding - Declare commercial goods carried in accompanied baggage or small vehicles - GOV.UK"

  def submitPage[T](formData: T)(implicit webDriver: HtmlUnitDriver): Unit = {
    click.on(IdQuery(formData.toString))
    click.on(NameQuery("continue"))
  }
}

trait ReviewGoodsPage extends BaseUiSpec { this: Suite =>

  def goToReviewGoodsPagePage(journeyType: JourneyType): Assertion = {
    goto(path)
    pageTitle must startWith(messages(s"reviewGoods.$journeyType.title"))
    elementText(findByTagName("h1")) must startWith(messages(s"reviewGoods.$journeyType.heading"))
  }
}

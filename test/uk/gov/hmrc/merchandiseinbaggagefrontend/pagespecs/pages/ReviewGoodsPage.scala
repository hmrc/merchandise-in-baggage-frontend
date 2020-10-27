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
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, YesNo}

class ReviewGoodsPage(baseUrl: BaseUrl)(implicit webDriver: WebDriver) extends BasePage(baseUrl) {

  import WebBrowser._

  override val path: String = ReviewGoodsPage.path

  def mustRenderDetail(journey: DeclarationJourney) = patiently {
    val goodsSummaries = findAll(ClassNameQuery("govuk-summary-list"))
    goodsSummaries.size mustBe journey.goodsEntries.entries.size

    //TODO figure out a way to actually assert the content
  }

  def fillOutForm(input: YesNo) = {
    click on find(IdQuery(input.entryName)).get
  }

  def mustRedirectToSearchGoods(journey: DeclarationJourney) = {
    click on find(NameQuery("continue")).get
    readPath() mustBe SearchGoodsPage.path(journey.goodsEntries.entries.size + 1)
  }

  def mustRedirectToPaymentCalculation() = {
    click on find(NameQuery("continue")).get
    readPath() mustBe "/merchandise-in-baggage/payment-calculation"
  }

  def mustRedirectToInvalidRequest() =
    readPath() mustBe "/merchandise-in-baggage/invalid-request"
}

object ReviewGoodsPage {
  val path: String = "/merchandise-in-baggage/review-goods"
}

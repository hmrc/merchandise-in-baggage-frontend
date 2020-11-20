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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages

import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo

import scala.collection.JavaConverters._
import scala.collection.mutable

class ReviewGoodsPage(implicit webDriver: WebDriver) extends BasePage {

  import WebBrowser._

  def goodsSummaries: Seq[Element] = findAll(ClassNameQuery("govuk-summary-list")).toSeq

  def rows(goodsSummary: Element): mutable.Seq[WebElement] =
    goodsSummary.underlying.findElements(By.className("govuk-summary-list__row")).asScala

  def goodsSummariesAsMap: Seq[Map[String, String]] = patiently {
    goodsSummaries.map { goodsSummary =>
      rows(goodsSummary).map { row =>
        row.findElement(By.className("govuk-summary-list__key")).getText ->
          row.findElement(By.className("govuk-summary-list__value")).getText
      }.toMap
    }
  }

  def remove(index: Int): String = patiently {
    val goodsSummary = goodsSummaries(index)

    val removeLinkRow = rows(goodsSummary).filter { row =>
      row.findElement(By.className("govuk-summary-list__key")).getText == "Remove"
    }.head

    val removeLink = removeLinkRow.findElement(By.tagName("a"))

    click on removeLink

    readPath()
  }


  def completeAndSubmitForm(input: YesNo): String = {
    click on find(IdQuery(input.entryName)).get
    click on find(NameQuery("continue")).get
    readPath()
  }
}

object ReviewGoodsPage {
  val path: String = "/merchandise-in-baggage/review-goods"
  val title = "Review your goods"
}

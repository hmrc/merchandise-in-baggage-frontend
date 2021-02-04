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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs

import com.softwaremill.macwire.wire
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.ReviewGoodsPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages._

class ReviewGoodsPageSpec extends BasePageSpec[ReviewGoodsPage] {
  override lazy val page: ReviewGoodsPage = wire[ReviewGoodsPage]

  private val requiredAnswerValidationMessage = "Select yes if you want to declare more goods"

  private val declarationBreakDown =
    Map("Price paid" -> "£99.99", "Type of goods" -> "wine", "Destination" -> "France", "VAT rate" -> "20%", "Number of items" -> "1")

  private val importDeclarationBreakDown = declarationBreakDown
    .+("Price paid" -> "99.99, Euro (EUR)")
    .-("Destination")
    .+("Produced in EU" -> "Yes")

  "the review goods page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageThatRequiresAtLeastOneCompletedGoodsEntry(path)
    behave like aPageWithABackButton(path, givenAGoodsEntryIsComplete(), PurchaseDetailsPage.path(1), shouldGoCya = false)
    behave like aPageWithABackButton(path, givenTwoGoodsEntriesAreComplete(), PurchaseDetailsPage.path(2), shouldGoCya = false)
    behave like aPageWithARequiredQuestion(path, requiredAnswerValidationMessage, givenAGoodsEntryIsComplete())

    "render correctly" when {
      "a single import goods entry is complete" in {
        givenADeclarationJourney(importJourneyWithOneCompleteGoodsEntry)
        open(path)

        page.headerText() mustBe title
        page.goodsSummariesAsMap mustBe Seq(importDeclarationBreakDown)
      }

      "multiple import goods entries are complete" in {
        givenADeclarationJourney(importJourneyWithTwoCompleteGoodsEntries)
        open(path)

        page.headerText() mustBe title

        page.goodsSummariesAsMap mustBe
          Seq(
            importDeclarationBreakDown,
            importDeclarationBreakDown
              .+("Price paid" -> "199.99, Euro (EUR)", "Type of goods" -> "cheese", "Number of items" -> "3")
          )
      }
    }

    "enable the user to remove goods" when {
      "there is a single goods entry" in {
        givenADeclarationJourney(importJourneyWithOneCompleteGoodsEntry)
        open(path)

        page.remove(0) mustBe RemoveGoodsPage.path(1)
      }

      "there are multiple goods entries" in {
        givenADeclarationJourney(importJourneyWithTwoCompleteGoodsEntries)
        open(path)

        page.remove(1) mustBe RemoveGoodsPage.path(2)
      }
    }

    s"redirect to /search-goods/:idx" when {
      "the user elects to make a second goods entry" in {
        givenADeclarationJourney(importJourneyWithOneCompleteGoodsEntry)
        open(path)

        page.completeAndSubmitForm(Yes) mustBe GoodsTypeQuantityPage.path(2)
      }

      "the user elects to make a third goods entry" in {
        givenADeclarationJourney(importJourneyWithTwoCompleteGoodsEntries)
        open(path)

        page.completeAndSubmitForm(Yes) mustBe GoodsTypeQuantityPage.path(3)
      }
    }

    s"redirect to /tax-calculation" when {
      "the user elects not to make another goods entry" in {
        givenADeclarationJourney(importJourneyWithOneCompleteGoodsEntry)

        open(path)
        page.completeAndSubmitForm(No) mustBe PaymentCalculationPage.path
      }
    }
  }
}

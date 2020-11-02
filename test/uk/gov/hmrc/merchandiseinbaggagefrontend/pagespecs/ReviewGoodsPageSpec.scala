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

package uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.ReviewGoodsPage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{PaymentCalculationPage, RemoveGoodsPage, ReviewGoodsPage, SearchGoodsPage}

class ReviewGoodsPageSpec extends BasePageSpec[ReviewGoodsPage] {
  override lazy val page: ReviewGoodsPage = reviewGoodsPage

  "the review goods page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageThatRequiresACompletedGoodsEntry(path)

    "render correctly" when {
      "a single goods entry is complete" in {
        givenADeclarationJourney(importJourneyWithOneCompleteGoodsEntry)
        open(path)

        reviewGoodsPage.mustRenderBasicContent(path, title)

        reviewGoodsPage.goodsSummariesAsMap mustBe
          Seq(
            Map(
              "Price paid" -> "99.99, Eurozone Euro (EUR)",
              "Type of goods" -> "wine",
              "Country" -> "France",
              "VAT Rate" -> "20%",
              "Remove" -> "",
              "Number of items" -> "1",
              "Invoice number" -> "1234560"))
      }

      "multiple goods entries are complete" in {
        givenADeclarationJourney(importJourneyWithTwoCompleteGoodsEntries)
        open(path)

        reviewGoodsPage.mustRenderBasicContent(path, title)

        reviewGoodsPage.goodsSummariesAsMap mustBe
          Seq(
            Map(
              "Price paid" -> "99.99, Eurozone Euro (EUR)",
              "Type of goods" -> "wine",
              "Country" -> "France",
              "VAT Rate" -> "20%",
              "Remove" -> "",
              "Number of items" -> "1",
              "Invoice number" -> "1234560"),
            Map(
              "Price paid" -> "199.99, Eurozone Euro (EUR)",
              "Type of goods" -> "cheese",
              "Country" -> "France",
              "VAT Rate" -> "20%",
              "Remove" -> "",
              "Number of items" -> "3",
              "Invoice number" -> "1234560"))
      }
    }

    "enable the user to remove goods" when {
      "there is a single goods entry" in {
        givenADeclarationJourney(importJourneyWithOneCompleteGoodsEntry)
        open(path)

        reviewGoodsPage.remove(0) mustBe RemoveGoodsPage.path(1)
      }

      "there are multiple goods entries" in {
        givenADeclarationJourney(importJourneyWithTwoCompleteGoodsEntries)
        open(path)

        reviewGoodsPage.remove(1) mustBe RemoveGoodsPage.path(2)
      }
    }

    s"redirect to /search-goods/:idx" when {
      "the user elects to make a second goods entry" in {
        givenADeclarationJourney(importJourneyWithOneCompleteGoodsEntry)
        open(path)

        reviewGoodsPage.completeAndSubmitForm(Yes) mustBe SearchGoodsPage.path(2)
      }

      "the user elects to make a third goods entry" in {
        givenADeclarationJourney(importJourneyWithTwoCompleteGoodsEntries)
        open(path)

        reviewGoodsPage.completeAndSubmitForm(Yes) mustBe SearchGoodsPage.path(3)
      }
    }

    s"redirect to /tax-calculation" when {
      "the user elects not to make another goods entry" in {
        givenADeclarationJourney(importJourneyWithOneCompleteGoodsEntry)

        open(path)
        reviewGoodsPage.completeAndSubmitForm(No) mustBe PaymentCalculationPage.path
      }
    }
  }
}

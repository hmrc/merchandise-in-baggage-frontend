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
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{InvalidRequestPage, ReviewGoodsPage}

class ReviewGoodsPageSpec extends BasePageSpec[ReviewGoodsPage] {
  override lazy val page: ReviewGoodsPage = reviewGoodsPage

  private val expectedTitle = "Review your goods"

  "the review goods page" should {
    behave like aPageWhichRequiresADeclarationJourney()

    "render correctly" when {
      "the goods entries are all complete" in {
        givenACompleteDeclarationJourney()

        reviewGoodsPage.open()
        reviewGoodsPage.mustRenderBasicContent(expectedTitle)
        reviewGoodsPage.mustRenderDetail(completedDeclarationJourney)
      }
    }

    s"redirect to ${InvalidRequestPage.path}" when {
      "there are incomplete goods entries" in {
        givenADeclarationJourney(declarationJourneyWithStartedGoodsEntry)

        reviewGoodsPage.open() mustBe InvalidRequestPage.path
      }
    }

    s"redirect to /search-goods/:idx" when {
      "Yes is selected" in {
        givenACompleteDeclarationJourney()

        reviewGoodsPage.open()
        reviewGoodsPage.fillOutForm(Yes)
        reviewGoodsPage.mustRedirectToSearchGoods(completedDeclarationJourney)
      }
    }

    s"redirect to /tax-calculation" when {
      "No is selected" in {
        givenACompleteDeclarationJourney()

        reviewGoodsPage.open()
        reviewGoodsPage.fillOutForm(No)
        reviewGoodsPage.mustRedirectToTaxCalculation()
      }
    }
  }
}

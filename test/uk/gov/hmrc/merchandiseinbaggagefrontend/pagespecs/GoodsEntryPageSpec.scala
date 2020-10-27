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

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{DeclarationDataCapturePage, ReviewGoodsPage}

import scala.concurrent.ExecutionContext.Implicits.global

trait GoodsEntryPageSpec[F, P <: DeclarationDataCapturePage[F]] extends BasePageSpec[P] {
  def givenAGoodsEntryIsStarted(): Unit = givenADeclarationJourney(importJourneyWithStartedGoodsEntry)

  def givenASecondGoodsEntryIsStarted(): Unit =
    givenADeclarationJourney(importJourneyWithOneCompleteAndOneStartedGoodsEntry)

  def givenAGoodsEntryIsComplete(): Unit = givenADeclarationJourney(importJourneyWithOneCompleteGoodsEntry)

  def givenTwoGoodsEntriesAreComplete(): Unit = givenADeclarationJourney(importJourneyWithTwoCompleteGoodsEntries)

  def extractFormDataFrom(declarationJourney: DeclarationJourney, index: Int): Option[F] =
    extractFormDataFrom(declarationJourney.goodsEntries.entries(index -1))

  def extractFormDataFrom(goodsEntry: GoodsEntry): Option[F]

  def aGoodsEntryPage(path: Int => String, title: String, formData: F, maybeExpectedRedirect: Option[Int => String]): Unit = {
    behave like aPageWhichRequiresADeclarationJourney(path(1))
    behave like aPageWhichRequiresADeclarationJourney(path(2))
    behave like aPageWhichRenders(path(1), givenAGoodsEntryIsStarted(), title)
    behave like aPageWhichRenders(path(2), givenASecondGoodsEntryIsStarted(), title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path(1), index = 1, givenAGoodsEntryIsComplete())
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path(2), index = 2, givenTwoGoodsEntriesAreComplete())

    maybeExpectedRedirect.foreach{ expectedRedirect =>
      behave like aDataCapturePageWithConditionalRouting(path(1), 1, givenAGoodsEntryIsStarted(), formData, expectedRedirect(1))
      behave like aDataCapturePageWithConditionalRouting(path(2), 2, givenASecondGoodsEntryIsStarted(), formData, expectedRedirect(2))
    }

    behave like aDataCapturePageWithConditionalRouting(path(1), 1, givenAGoodsEntryIsComplete(), formData, ReviewGoodsPage.path)
    behave like aDataCapturePageWithConditionalRouting(path(2), 2, givenTwoGoodsEntriesAreComplete(), formData, ReviewGoodsPage.path)
  }

  def aPageWhichDisplaysPreviouslyEnteredAnswers(path: String, index: Int, setup: => Unit = givenADeclarationJourney(completedDeclarationJourney)): Unit =
    s"render correctly om $path" when {
      "a declaration has been completed" in {
        val expectedData = extractFormDataFrom(completedDeclarationJourney, index).get
        setup

        open(path)
        page.previouslyEnteredValuesAreDisplayed(expectedData)
      }
    }

  def aDataCapturePageWithConditionalRouting(path: String, index: Int, setUp: => Unit = Unit, formData: F, expectedRedirectPath: String): Unit = {
    s"redirect from $path to $expectedRedirectPath" when {
      s"the form is filled with $formData" in {
        submitAndEnsurePersistence(path, setUp, formData, index) mustBe expectedRedirectPath
      }
    }
  }

  def submitAndEnsurePersistence(path: String, setUp: => Unit = Unit, formData: F, index: Int): String = {
    def ensurePersistence = {
      val persistedJourneys = declarationJourneyRepository.findAll().futureValue
      persistedJourneys.size mustBe 1
      extractFormDataFrom(persistedJourneys.head, index) mustBe Some(formData)
    }

    setUp
    open(path)
    page.fillOutForm(formData)
    val redirectPath = page.clickOnCTA()

    ensurePersistence

    redirectPath
  }
}

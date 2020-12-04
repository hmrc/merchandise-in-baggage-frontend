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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs

import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{CheckYourAnswersPage, DeclarationDataCapturePage, ReviewGoodsPage}

import scala.concurrent.ExecutionContext.Implicits.global

trait GoodsEntryPageSpec[F, P <: DeclarationDataCapturePage[F]] extends BasePageSpec[P] {
  def givenAGoodsEntryIsStarted(): Unit = givenADeclarationJourney(importJourneyWithStartedGoodsEntry)

  def extractFormDataFrom(declarationJourney: DeclarationJourney, index: Int): Option[F] =
    extractFormDataFrom(declarationJourney.goodsEntries.entries(index - 1))

  def extractFormDataFrom(goodsEntry: GoodsEntry): Option[F]

  def aGoodEntryExportPageTitle(path: String, title: String) =
    behave like aPageWhichRenders(path, givenADeclarationJourney(importJourneyWithStartedGoodsEntry.copy(declarationType = Export)), title)

  def aGoodsEntryPage(path: Int => String,
                      title: String,
                      formData: F,
                      maybeExpectedRedirect: Option[Int => String],
                      expectedBackPath: Int => String,
                      cyaRouting: Boolean = true): Unit = {
    behave like aPageWhichRequiresADeclarationJourney(path(1))
    behave like aPageWhichRequiresADeclarationJourney(path(2))
    behave like aPageWhichRenders(path(1), givenAGoodsEntryIsStarted(), title)
    behave like aPageWhichRenders(path(2), givenASecondGoodsEntryIsStarted(), title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path(1), index = 1, givenAGoodsEntryIsComplete())
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path(2), index = 2, givenTwoGoodsEntriesAreComplete())

    maybeExpectedRedirect.foreach { expectedRedirect =>
      behave like aDataCapturePageWithConditionalRouting(path(1), 1, givenAGoodsEntryIsStarted(), formData, expectedRedirect(1))
      behave like aDataCapturePageWithConditionalRouting(path(2), 2, givenASecondGoodsEntryIsStarted(), formData, expectedRedirect(2))

      behave like
        aDataCapturePageWithConditionalRouting(
          path(3),
          3,
          givenADeclarationJourney(previouslyCompleteJourneyWithIncompleteGoodsEntryAdded),
          formData,
          expectedRedirect(3))
    }

    behave like aDataCapturePageWithConditionalRouting(path(1), 1, givenAGoodsEntryIsComplete(), formData, ReviewGoodsPage.path)
    behave like aDataCapturePageWithConditionalRouting(path(2), 2, givenTwoGoodsEntriesAreComplete(), formData, ReviewGoodsPage.path)

    if(cyaRouting) {
      behave like aPageWhichRedirectsToCheckYourAnswersIfTheDeclarationIsComplete(path(1), 1, formData)
      behave like aPageWhichRedirectsToCheckYourAnswersIfTheDeclarationIsComplete(path(2), 2, formData)
    }

    behave like aPageWithABackButton(path(1), givenAGoodsEntryIsStarted(), expectedBackPath(1))
    behave like aPageWithABackButton(path(2), givenASecondGoodsEntryIsStarted(), expectedBackPath(2))
  }

  def aPageWhichRedirectsToCheckYourAnswersIfTheDeclarationIsComplete(path: String, index: Int, formData: F): Unit =
    s"redirect from $path to ${CheckYourAnswersPage.path}" when {
      s"the declaration is completed with $formData" in {
        submitAndEnsurePersistence(path, givenACompleteDeclarationJourney(), formData, index) mustBe CheckYourAnswersPage.path
      }
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

  def aPageWithValidation(path: String,
                          setUp: => Unit,
                          formData: F,
                          validationMessage: String,
                          validationMessageFieldId: String = "value-error"): Unit =
    s"display the validation message [$validationMessage] with id [$validationMessageFieldId] for form data [$formData]" when {
      "the user attempts to submit the form without answering the question" in {
        setUp
        open(path)
        page.fillOutForm(formData)
        page.clickOnCTA() mustBe path
        page.validationMessage(validationMessageFieldId) mustBe validationMessage
      }
    }

}

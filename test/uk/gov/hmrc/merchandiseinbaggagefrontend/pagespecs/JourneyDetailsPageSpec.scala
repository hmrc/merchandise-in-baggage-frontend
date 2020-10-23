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

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, JourneyDetailsEntry}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CheckYourAnswersPage, GoodsInVehiclePage, InvalidRequestPage, JourneyDetailsPage}

class JourneyDetailsPageSpec extends DeclarationDataCapturePageSpec[JourneyDetailsEntry, JourneyDetailsPage] with ScalaFutures {
  override lazy val page: JourneyDetailsPage = journeyDetailsPage

  private val expectedTitle = "Journey details"

  "the journey details page" should {
    behave like aPageWhichRenders(givenAnImportJourneyIsStarted(), expectedTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers()
    behave like aPageWhichRequiresADeclarationJourney()
    behave like aDataCapturePageWithConditionalRouting(givenACompleteDeclarationJourney(), heathrowJourneyEntry, CheckYourAnswersPage.path)
    behave like aDataCapturePageWithConditionalRouting(givenACompleteDeclarationJourney(), doverJourneyEntry, GoodsInVehiclePage.path)
    behave like aDataCapturePageWithConditionalRouting(givenAnImportJourneyIsStarted(), heathrowJourneyEntry, InvalidRequestPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[JourneyDetailsEntry] =
    declarationJourney.maybeJourneyDetailsEntry
}

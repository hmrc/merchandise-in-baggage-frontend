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
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, JourneyDetailsEntry}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.JourneyDetailsPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages._

class JourneyDetailsPageSpec extends DeclarationDataCapturePageSpec[JourneyDetailsEntry, JourneyDetailsPage] with ScalaFutures {
  override lazy val page: JourneyDetailsPage = wire[JourneyDetailsPage]

  private val validationMessages =
    Set("Select your place of arrival in Great Britain", "Date must include a day", "Date must include a month", "Date must include a year")

  "the journey details page" should {
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWhichDisplaysValidationErrorMessagesInTheErrorSummary(path, validationMessages, givenAnImportJourneyIsStarted())
    behave like aDataCapturePageWithConditionalRouting(
      path,
      givenACompleteDeclarationJourney(),
      heathrowJourneyEntry,
      CheckYourAnswersPage.path)
    behave like aDataCapturePageWithConditionalRouting(path, givenAnImportJourneyIsStarted(), doverJourneyEntry, GoodsInVehiclePage.path)
    behave like aPageWhichRedirectsToCheckYourAnswersIfTheDeclarationIsComplete(path, doverJourneyEntry)
    behave like aPageWithABackButton(path, givenAnImportJourneyIsStarted(), EnterEmailPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[JourneyDetailsEntry] =
    declarationJourney.maybeJourneyDetailsEntry
}

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

import com.softwaremill.macwire.wire
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, Name}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.TravellerDetailsPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{EnterEmailPage, EoriNumberPage, TravellerDetailsPage}

class TravellerDetailsPageSpec extends DeclarationDataCapturePageSpec[Name, TravellerDetailsPage] with ScalaFutures {
  override lazy val page: TravellerDetailsPage = wire[TravellerDetailsPage]

  private val requiredFirstNameValidationMessage = "Enter the first name of the person carrying the goods"
  private val requiredLastNameValidationMessage = "Enter the last name of the person carrying the goods"

  "the traveller details page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWithARequiredQuestion(path, requiredFirstNameValidationMessage, givenAnImportJourneyIsStarted(), "firstName-error")
    behave like aPageWithARequiredQuestion(path, requiredLastNameValidationMessage, givenAnImportJourneyIsStarted(), "lastName-error")
    behave like aDataCapturePageWithSimpleRouting(path, givenAnImportJourneyIsStarted(), Seq(Name("Terry", "Test")), EnterEmailPage.path)
    behave like aPageWithABackButton(path, givenAnAgentJourney(), EoriNumberPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[Name] =
    declarationJourney.maybeNameOfPersonCarryingTheGoods
}

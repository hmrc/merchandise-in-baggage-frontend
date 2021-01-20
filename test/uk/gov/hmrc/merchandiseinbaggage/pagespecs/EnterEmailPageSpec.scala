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
import uk.gov.hmrc.merchandiseinbaggage.model.api.Email
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.EnterEmailPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{EnterEmailPage, JourneyDetailsPage, TravellerDetailsPage}

class EnterEmailPageSpec extends DeclarationDataCapturePageSpec[Email, EnterEmailPage] with ScalaFutures {
  override lazy val page: EnterEmailPage = wire[EnterEmailPage]

  private val requiredAnswerValidationMessage = "Enter an email address"
  private val invalidMessage = "Enter an email address in the correct format, like name@example.com"
  private val emailValidationErrorField = "email-error"

  private val invalidEmail = Email("invalidEmail")

  "the enter page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aDataCapturePageWithSimpleRouting(
      path,
      givenAnImportJourneyIsStarted(),
      Seq(Email("test@test.com")),
      JourneyDetailsPage.path)
    behave like aPageWithARequiredQuestion(
      path,
      requiredAnswerValidationMessage,
      givenAnImportJourneyIsStarted(),
      emailValidationErrorField)

    behave like
      aPageWithValidation(path, givenAnImportJourneyIsStarted(), invalidEmail, invalidMessage, emailValidationErrorField)

    behave like aPageWhichDisplaysValidationErrorMessagesInTheErrorSummary(
      path,
      Set(requiredAnswerValidationMessage),
      givenAnImportJourneyIsStarted())

    behave like aPageWithABackButton(path, givenAnAgentJourney(), TravellerDetailsPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[Email] =
    declarationJourney.maybeEmailAddress
}

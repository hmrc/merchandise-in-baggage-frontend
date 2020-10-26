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

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CheckYourAnswersPage, InvalidRequestPage, VehicleRegistrationNumberPage}

class VehicleRegistrationNumberPageSpec extends DeclarationDataCapturePageSpec[String, VehicleRegistrationNumberPage] {
  override lazy val page: VehicleRegistrationNumberPage = vehicleRegistrationNumberPage

  private val expectedTitle = "What is the registration number of the vehicle?"
  private val registrationNumber = "reg 123"

  "the page" should {
    behave like aPageWhichRequiresADeclarationJourney()
    behave like aPageWhichRenders(givenAnImportJourneyIsStarted(), expectedTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers()
    behave like aDataCapturePageWithConditionalRouting(givenACompleteDeclarationJourney(), registrationNumber, CheckYourAnswersPage.path)
    behave like aDataCapturePageWithConditionalRouting(givenAnImportJourneyIsStarted(), registrationNumber, InvalidRequestPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[String] =
    declarationJourney.maybeRegistrationNumber
}

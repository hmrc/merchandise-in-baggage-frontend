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
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.GoodsInVehiclePage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{CheckYourAnswersPage, JourneyDetailsPage, RadioButtonPage, VehicleSizePage}

class GoodsInVehiclePageSpec extends DeclarationDataCapturePageSpec[YesNo, RadioButtonPage[YesNo]] {
  override lazy val page: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]

  private val requiredAnswerValidationMessage = "Select yes if the goods are arriving by vehicle"

  "the goods in vehicle page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWithARequiredQuestion(path, requiredAnswerValidationMessage, givenAnImportJourneyIsStarted())
    behave like aPageWhichRedirectsToCheckYourAnswersIfTheDeclarationIsComplete(path, Yes)
    behave like aDataCapturePageWithConditionalRouting(path, givenAnImportJourneyIsStarted(), Yes, VehicleSizePage.path)
    behave like aDataCapturePageWithConditionalRouting(
      path,
      givenADeclarationJourney(completedDeclarationJourney.copy(maybeTravellingByVehicle = None)),
      No,
      CheckYourAnswersPage.path)
    behave like aPageWithABackButton(path, givenAnImportJourneyIsStarted(), JourneyDetailsPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[YesNo] = declarationJourney.maybeTravellingByVehicle
}

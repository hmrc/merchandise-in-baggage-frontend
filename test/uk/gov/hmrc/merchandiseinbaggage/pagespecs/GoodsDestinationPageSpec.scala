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
import uk.gov.hmrc.merchandiseinbaggage.model.core.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsDestination}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.GoodsDestinationPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{ExciseAndRestrictedGoodsPage, GoodsRouteDestinationPage, RadioButtonPage}

class GoodsDestinationPageSpec extends DeclarationDataCapturePageSpec[GoodsDestination, RadioButtonPage[GoodsDestination]] {
  override lazy val page: RadioButtonPage[GoodsDestination] = wire[RadioButtonPage[GoodsDestination]]

  private val answerRequiredValidationMessage = "Select which country in the UK the goods are going to"

  "the goods destination page" should {
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), importTitle)
    behave like aPageWhichRenders(path, givenAnExportJourneyIsStarted(), exportTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWithARequiredQuestion(path, answerRequiredValidationMessage, givenAnImportJourneyIsStarted())
    behave like aPageWhichRedirectsToCheckYourAnswersIfTheDeclarationIsComplete(path, NorthernIreland)

    behave like aDataCapturePageWithConditionalRouting(
        path, givenAnImportJourneyIsStarted(), NorthernIreland, GoodsRouteDestinationPage.path)
    behave like aDataCapturePageWithConditionalRouting(
      path, givenAnImportJourneyIsStarted(), GreatBritain, ExciseAndRestrictedGoodsPage.path)

    behave like aPageWithNoBackButton(path, givenAnImportJourneyIsStarted())
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[GoodsDestination] =
    declarationJourney.maybeGoodsDestination
}

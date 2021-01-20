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
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.GoodsRouteDestinationPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages._

class GoodsRouteDestinationPageSpec extends DeclarationDataCapturePageSpec[YesNo, RadioButtonPage[YesNo]] {
  override lazy val page: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]

  val answerRequiredValidationMessage = "Select yes if the final destination of the goods is the Republic of Ireland"

  "the goods route destination page" should {
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), importTitle)
    behave like aPageWhichRenders(path, givenAnExportJourneyIsStarted(), exportTitle)
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWithARequiredQuestion(path, answerRequiredValidationMessage, givenAnImportJourneyIsStarted())

    behave like aDataCapturePageWithConditionalRouting(path, givenAnImportJourneyIsStarted(), Yes, CannotUseServiceIrelandPage.path)
    behave like aDataCapturePageWithConditionalRouting(
      path,
      givenAnImportJourneyIsStarted(),
      No,
      ExciseAndRestrictedGoodsPage.path,
      "and declarationType is Import")
    behave like aDataCapturePageWithConditionalRouting(
      path,
      givenAnExportJourneyIsStarted(),
      Yes,
      NoDeclarationNeededPage.path,
      "and declarationType is Export")
    behave like aDataCapturePageWithConditionalRouting(
      path,
      givenAnExportJourneyIsStarted(),
      No,
      ExciseAndRestrictedGoodsPage.path,
      "and declarationType is Export")
    behave like aPageWithABackButton(path, givenAnImportJourneyIsStarted(), GoodsDestinationPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[YesNo] =
    declarationJourney.maybeImportOrExportGoodsFromTheEUViaNorthernIreland
}

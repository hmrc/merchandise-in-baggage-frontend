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

import com.softwaremill.macwire.wire
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, YesNo}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.GoodsRouteDestinationPage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages._

class GoodsRouteDestinationPageSpec extends DeclarationDataCapturePageSpec[YesNo, RadioButtonPage[YesNo]] {
  override lazy val page: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]

  "the goods route destination page" should {
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), importTitle)
    behave like aPageWhichRenders(path, givenAnExportJourneyIsStarted(), exportTitle)
    behave like aPageWhichRequiresADeclarationJourney(path)

    behave like aDataCapturePageWithConditionalRoutingWithoutPersistence(
      path, givenAnImportJourneyIsStarted(), Yes, CannotUseServiceIrelandPage.path)
    behave like aDataCapturePageWithConditionalRoutingWithoutPersistence(
      path, givenAnImportJourneyIsStarted(), No, ExciseAndRestrictedGoodsPage.path, "and declarationType is Import")
    behave like aDataCapturePageWithConditionalRoutingWithoutPersistence(
      path, givenAnExportJourneyIsStarted(), Yes, NoDeclarationNeededPage.path)
    behave like aDataCapturePageWithConditionalRoutingWithoutPersistence(
      path, givenAnExportJourneyIsStarted(), No, ExciseAndRestrictedGoodsPage.path, "and declarationType is Export")
    behave like aPageWithABackButton(path, givenAnImportJourneyIsStarted(), GoodsDestinationPage.path)
  }

  //TODO never used, come up with a better way of handling pages that don't have persistence
  // PH - I think this should be persisted on the DeclarationJourney
  // and we should check the user has answered the question before /check-your-answers
  // otherwise the user could force browse and avoid answering the question, or submit a declaration for an invalid case
  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[YesNo] = Some(Yes)
}

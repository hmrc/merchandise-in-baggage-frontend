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
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.ValueWeightOfGoodsPage.{greatBritainTitle, northernIrelandTitle, path}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CannotUseServicePage, ExciseAndRestrictedGoodsPage, GoodsTypeQuantityPage, RadioButtonPage}

class ValueWeightOfGoodsPageSpec extends DeclarationDataCapturePageSpec[YesNo, RadioButtonPage[YesNo]] {
  override lazy val page: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]

  "the value and weight of goods page" should {
    behave like aPageWhichRenders(path, givenAnImportToNorthernIrelandJourneyIsStarted(), northernIrelandTitle)
    behave like aPageWhichRenders(path, givenAnImportToGreatBritainJourneyIsStarted(), greatBritainTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWhichRequiresADeclarationJourney(path)

    behave like aDataCapturePageWithConditionalRouting(
      path, givenAnImportToGreatBritainJourneyIsStarted(), No, GoodsTypeQuantityPage.path(1))

    behave like aDataCapturePageWithConditionalRouting(
      path, givenAnImportToGreatBritainJourneyIsStarted(), Yes, CannotUseServicePage.path)

    behave like aPageWhichRedirectsToCheckYourAnswersIfTheDeclarationIsComplete(path, No)
    behave like aPageWithABackButton(path, givenAnImportToNorthernIrelandJourneyIsStarted(), ExciseAndRestrictedGoodsPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[YesNo] =
    declarationJourney.maybeValueWeightOfGoodsExceedsThreshold
}

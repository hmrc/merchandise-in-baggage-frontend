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

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, GoodsDestination}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.GoodsDestinationPage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{ExciseAndRestrictedGoodsPage, GoodsRouteDestinationPage, RadioButtonPage}

class GoodsDestinationPageSpec extends DeclarationDataCapturePageSpec[GoodsDestination, RadioButtonPage[GoodsDestination]] {
  override lazy val page: RadioButtonPage[GoodsDestination] = goodsDestinationPage

  "the goods destination page" should {
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWhichRequiresADeclarationJourney(path)

    behave like aDataCapturePageWithConditionalRouting(
        path, givenAnImportJourneyIsStarted(), NorthernIreland, GoodsRouteDestinationPage.path)
    behave like aDataCapturePageWithConditionalRouting(
      path, givenAnImportJourneyIsStarted(), GreatBritain, ExciseAndRestrictedGoodsPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[GoodsDestination] =
    declarationJourney.maybeGoodsDestination
}

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

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, GoodsDestination, GoodsDestinations}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{GoodsDestinationPage, ValueWeightOfGoodsPage}

class GoodsDestinationPageSpec extends DeclarationDataCapturePageSpec[GoodsDestination, GoodsDestinationPage] {
  override lazy val page: GoodsDestinationPage = goodsDestinationPage

  private val expectedTitle = "Where in the UK are the goods going?"

  "the goods destination page" should {
    behave like aPageWhichRenders(givenAnImportJourneyIsStarted(), expectedTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers()
    behave like aPageWhichRequiresADeclarationJourney()
    behave like aDataCapturePageWithSimpleRouting(givenAnImportJourneyIsStarted(), GoodsDestinations.values, ValueWeightOfGoodsPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[GoodsDestination] =
    declarationJourney.maybeGoodsDestination
}

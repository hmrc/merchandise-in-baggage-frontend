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

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, GoodsVatRate}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.GoodsVatRatePage

class GoodsVatRatePageSpec extends DeclarationDataCapturePageSpec[GoodsVatRate, GoodsVatRatePage] {
  override lazy val page: GoodsVatRatePage = goodsVatRatePage

  private val expectedTitle = "Check which VAT rate applies to the test good"

  "the goods vat rate page" should {
    behave like aPageWhichRenders(givenADeclarationJourney(declarationJourneyWithStartedGoodsEntry), expectedTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers()
    behave like aPageWhichRequiresADeclarationJourney()
    //behave like aDataCapturePageWithSimpleRouting(givenACompleteDeclarationJourney(), Twenty, SearchGoodsCountryPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[GoodsVatRate] =
    declarationJourney.goodsEntries.entries.head.maybeGoodsVatRate
}

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

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, YesNo}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CannotUseServicePage, ExciseAndRestrictedGoodsPage, GoodsDestinationPage}

class ExciseAndRestrictedGoodsPageSpec extends DeclarationDataCapturePageSpec[YesNo, ExciseAndRestrictedGoodsPage] {
  override lazy val page: ExciseAndRestrictedGoodsPage = exciseAndRestrictedGoodsPage

  private val expectedTitle = "Are you bringing in excise goods or restricted goods?"

  "the excise and restricted goods page" should {
    behave like aPageWhichRenders(givenAnImportJourneyIsStarted(), expectedTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers()
    behave like aPageWhichRequiresADeclarationJourney()
    behave like aPageWithConditionalRouting(givenAnImportJourneyIsStarted(), No, GoodsDestinationPage.path)
    behave like aPageWithConditionalRouting(givenAnImportJourneyIsStarted(), Yes, CannotUseServicePage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[YesNo] =
    declarationJourney.maybeExciseOrRestrictedGoods
}

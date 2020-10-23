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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, YesNo}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CannotUseServicePage, SearchGoodsPage, ValueWeightOfGoodsPage}

class ValueWeightOfGoodsPageSpec extends DeclarationDataCapturePageSpec[YesNo, ValueWeightOfGoodsPage] {
  override lazy val page: ValueWeightOfGoodsPage = valueWeightOfGoodsPage

  private val northernIrelandTitle = "Is the total value of the goods over £873 or 1000 kilograms (kg)?"
  private val greatBritainTitle = "Is the total value of the goods over £1500 or 1000 kilograms (kg)?"

  private def givenANorthernIrelandJourney(): Unit =
    givenADeclarationJourney(startedDeclarationJourney.copy(maybeGoodsDestination = Some(NorthernIreland)))
  private def givenAGreatBritainJourney(): Unit =
    givenADeclarationJourney(startedDeclarationJourney.copy(maybeGoodsDestination = Some(GreatBritain)))

  "the excise and restricted goods page" should {
    behave like aPageWhichRenders(givenANorthernIrelandJourney(), northernIrelandTitle)
    behave like aPageWhichRenders(givenAGreatBritainJourney(), greatBritainTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers()
    behave like aPageWhichRequiresADeclarationJourney()
    behave like aPageWithConditionalRouting(givenACompleteDeclarationJourney(), No, SearchGoodsPage.path)
    behave like aPageWithConditionalRouting(givenACompleteDeclarationJourney(), Yes, CannotUseServicePage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[YesNo] =
    declarationJourney.maybeValueWeightOfGoodsExceedsThreshold
}

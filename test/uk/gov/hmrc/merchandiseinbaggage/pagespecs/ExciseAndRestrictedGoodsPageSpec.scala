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
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.ExciseAndRestrictedGoodsPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages._

class ExciseAndRestrictedGoodsPageSpec extends DeclarationDataCapturePageSpec[YesNo, RadioButtonPage[YesNo]] {
  override lazy val page: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]

  private def setup(): Unit = givenAnImportToGreatBritainJourneyIsStarted()

  private val answerRequiredValidationMessage = "Select yes if you are bringing in excise, controlled or restricted goods"

  "the excise and restricted goods page" should {
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), importTitle)
    behave like aPageWhichRenders(path, givenAnExportJourneyIsStarted(), exportTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWithARequiredQuestion(path, answerRequiredValidationMessage, givenAnImportJourneyIsStarted())
    behave like aDataCapturePageWithConditionalRouting(path, setup(), No, ValueWeightOfGoodsPage.path)
    behave like aDataCapturePageWithConditionalRouting(path, setup(), Yes, CannotUseServicePage.path)
    behave like aPageWhichRedirectsToCheckYourAnswersIfTheDeclarationIsComplete(path, No)
    behave like aPageWithABackButton(path, setup(), GoodsDestinationPage.path)
    behave like aPageWithABackButton(path, givenAnImportToNorthernIrelandJourneyIsStarted(), GoodsRouteDestinationPage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[YesNo] =
    declarationJourney.maybeExciseOrRestrictedGoods
}

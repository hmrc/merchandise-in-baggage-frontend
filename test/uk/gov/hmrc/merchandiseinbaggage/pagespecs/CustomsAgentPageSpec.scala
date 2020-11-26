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
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.CustomsAgentPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{AgentDetailsPage, EoriNumberPage, PaymentCalculationPage, RadioButtonPage}

class CustomsAgentPageSpec extends DeclarationDataCapturePageSpec[YesNo, RadioButtonPage[YesNo]] with CoreTestData with TaxCalculation {
  override lazy val page: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]

  private val requiredAnswerValidationMessage = "Select yes if you are a customs agent"

  "the page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), title)
    behave like aDataCapturePageWithConditionalRouting(path, setup(), Yes, AgentDetailsPage.path)
    behave like aDataCapturePageWithConditionalRouting(path, setup(), No, EoriNumberPage.path)
    behave like aPageWhichRedirectsToCheckYourAnswersIfTheDeclarationIsComplete(path, Yes)
    behave like aPageWithARequiredQuestion(path, requiredAnswerValidationMessage, givenAnImportJourneyIsStarted())

    behave like
      aPageWithABackButton(
        path, givenADeclarationWithTaxDue(importJourneyWithTwoCompleteGoodsEntries).futureValue, PaymentCalculationPage.path)
  }

  private def setup(): Unit =
    givenADeclarationJourney(startedImportJourney.copy(maybeIsACustomsAgent = Some(Yes)))

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[YesNo] =
    declarationJourney.maybeIsACustomsAgent
}

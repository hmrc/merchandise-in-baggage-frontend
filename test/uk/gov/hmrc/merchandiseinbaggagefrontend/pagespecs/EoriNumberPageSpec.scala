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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, Eori}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{EoriNumberPage, TravellerDetailsPage}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.EoriNumberPage._

class EoriNumberPageSpec extends DeclarationDataCapturePageSpec[Eori, EoriNumberPage] {
  override lazy val page: EoriNumberPage = eoriNumberPage

  private val eori = Eori("GB123467800000")

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[Eori] =
    declarationJourney.maybeEori

  private def givenAnAgentJourney(): Unit =
    givenADeclarationJourney(startedDeclarationJourney.copy(maybeIsACustomsAgent = Some(Yes)))

  private def givenANonAgentJourney(): Unit =
    givenADeclarationJourney(startedDeclarationJourney.copy(maybeIsACustomsAgent = Some(No)))

  "the eori number page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWhichRequiresACustomsAgentDeclaration(path)
    behave like aPageWhichRenders(path, givenAnAgentJourney(), expectedAgentTitle)
    behave like aPageWhichRenders(path, givenANonAgentJourney(), expectedNonAgentTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aDataCapturePageWithSimpleRouting(path, givenACompleteDeclarationJourney(), Seq(eori), TravellerDetailsPage.path)
  }
}

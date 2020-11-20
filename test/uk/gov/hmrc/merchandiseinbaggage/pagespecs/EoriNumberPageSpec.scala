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
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, Eori}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.EoriNumberPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{CustomsAgentPage, EoriNumberPage, TravellerDetailsPage}

class EoriNumberPageSpec extends DeclarationDataCapturePageSpec[Eori, EoriNumberPage] {
  override lazy val page: EoriNumberPage = wire[EoriNumberPage]

  private val eori = Eori("GB123467800000")

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[Eori] = declarationJourney.maybeEori

  "the eori number page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWhichRequiresACustomsAgentDeclaration(path)
    behave like aPageWhichRenders(path, givenAnAgentJourney(), expectedAgentTitle)
    behave like aPageWhichRenders(path, givenANonAgentJourney(), expectedNonAgentTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aDataCapturePageWithSimpleRouting(path, givenAnAgentJourney(), Seq(eori), TravellerDetailsPage.path)
    behave like aPageWithABackButton(path, givenAnAgentJourney(), CustomsAgentPage.path)
  }
}

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
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.AgentDetailsPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{AgentDetailsPage, CustomsAgentPage}

class AgentDetailsPageSpec extends DeclarationDataCapturePageSpec[String, AgentDetailsPage] {
  override lazy val page: AgentDetailsPage = wire[AgentDetailsPage]

  "the page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWhichRenders(path, givenAnImportJourneyIsStarted(), title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWithABackButton(path, givenAnImportJourneyIsStarted(), CustomsAgentPage.path)

    "allow the user to navigate to the /enter-agent-address" in {
      val redirectedPath = submitAndEnsurePersistence(path, givenAnImportJourneyIsStarted(), "test agent")

      val successfulRedirectDependingOnWhetherAddressLookupIsAvailable =
        redirectedPath == "/merchandise-in-baggage/enter-agent-address" || redirectedPath.startsWith("/lookup-address")
      successfulRedirectDependingOnWhetherAddressLookupIsAvailable mustBe true
    }
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[String] =
    declarationJourney.maybeCustomsAgentName
}

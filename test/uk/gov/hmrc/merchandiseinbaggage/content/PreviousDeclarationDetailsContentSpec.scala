/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.content

import org.openqa.selenium.By
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyOnFoot
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.PreviousDeclarationDetailsPage
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.{givenAPaymentCalculation, givenPersistedDeclarationIsFound}

import java.time.LocalDate

class PreviousDeclarationDetailsContentSpec extends PreviousDeclarationDetailsPage with CoreTestData {

  "it should show all paid goods" in {
    val journey = givenAJourneyWithSession(Amend)
    givenAPaymentCalculation(aCalculationResult)
    givenPersistedDeclarationIsFound(declarationWithPaidAmendment, journey.declarationId)
    goToPreviousDeclarationDetailsPage

    webDriver.findElements(By.xpath("//*[text()[contains(.,'Type of goods')]]")).size() mustBe 3
  }

  "it should not show unpaid goods" in {
    val journey = givenAJourneyWithSession(Amend)
    givenAPaymentCalculation(aCalculationResult)
    givenPersistedDeclarationIsFound(declarationWithAmendment, journey.declarationId)
    goToPreviousDeclarationDetailsPage

    webDriver.findElements(By.xpath("//*[text()[contains(.,'Type of goods')]]")).size() mustBe 2
  }

  "it should not show the 'Add more goods' button if the travel date was out side of allowed range" in {
    val journey = givenAJourneyWithSession(Amend)
    givenAPaymentCalculation(aCalculationResult)
    givenPersistedDeclarationIsFound(
      declarationWithAmendment.copy(journeyDetails = JourneyOnFoot(journeyPort, LocalDate.now().minusDays(35))),
      journey.declarationId
    )
    goToPreviousDeclarationDetailsPage

    webDriver.findElements(By.name("continue")).size() mustBe 0
  }

  "it should show allowance left" in {
    val journey = givenAJourneyWithSession(Amend)
    givenAPaymentCalculation(aCalculationResult)
    givenPersistedDeclarationIsFound(
      declarationWithAmendment.copy(journeyDetails = JourneyOnFoot(journeyPort, LocalDate.now())),
      journey.declarationId
    )
    goToPreviousDeclarationDetailsPage

    val expected =
      s"${messages("previousDeclarationDetails.allowance.p1")} £2,499.90 ${messages("previousDeclarationDetails.allowance.p2", thresholdValueInUI)}"

    findById("add_h2").getText mustBe messages("previousDeclarationDetails.add")
    findByClassName("govuk-inset-text").getText mustBe expected
  }
}

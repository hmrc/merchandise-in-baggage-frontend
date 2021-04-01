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

package uk.gov.hmrc.merchandiseinbaggage.smoketests

import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, Paid, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, RetrieveDeclaration}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.{NewOrExistingDeclarationPage, PreviousDeclarationDetailsPage, RetrieveDeclarationPage, StartExportPage}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._

import java.time.LocalDateTime

class AdditionalDeclarationExportSpec extends BaseUiSpec {

  "Additional Declaration Export journey - happy path" should {
    "work as expected" in {
      goto(StartExportPage.path)

      submitPage(NewOrExistingDeclarationPage, "Amend")

      val paidDeclaration = declaration.copy(paymentStatus = Some(Paid))

      givenFindByDeclarationReturnSuccess(mibReference, eori, paidDeclaration)

      val sessionId = SessionId()
      val created = LocalDateTime.now
      val id = paidDeclaration.declarationId
      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = sessionId,
          declarationType = DeclarationType.Export,
          maybeEori = Some(eori),
          createdAt = created,
          declarationId = id)

      givenADeclarationJourneyIsPersisted(exportJourney)
      givenPersistedDeclarationIsFound(exportJourney.declarationIfRequiredAndComplete.get, id)

      submitPage(RetrieveDeclarationPage, RetrieveDeclaration(mibReference, eori))

      webDriver.getCurrentUrl mustBe fullUrl(PreviousDeclarationDetailsPage.path)
    }
  }
}

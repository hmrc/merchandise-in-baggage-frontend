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

package uk.gov.hmrc.merchandiseinbaggage.content

import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.DeclarationConfirmationPage
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenPersistedDeclarationIsFound

class DeclarationConfirmationContentSpec extends DeclarationConfirmationPage with CoreTestData {

  "it should show all goods & full payment summary considering amendments" in {
    val journey = givenAJourneyWithSession()
    givenPersistedDeclarationIsFound(declarationWithPaidAmendment, journey.declarationId)
    goToConfirmationPage

    findById("category_0").getText mustBe "wine"

    findById("category_1").getText mustBe "cheese"

    findById("category_2").getText mustBe "wine"

    findById("customsDuty").getText mustBe "£2"
    findById("vat").getText mustBe "£2"
    findById("totalTax").getText mustBe "£2"
  }
}

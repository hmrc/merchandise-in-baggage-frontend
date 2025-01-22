/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.DeclarationNotFoundPage._
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.{DeclarationNotFoundPage, RetrieveDeclarationPage, StartImportPage}

class DeclarationNotFoundContentSpec extends DeclarationNotFoundPage with CoreTestData {

  "render content" in {
    givenAJourneyWithSession()
    goToDeclarationNotFoundPage

    pageTitle mustBe title
    elementText(findByTagName("h1")) mustBe messages("declarationNotFound.heading")
    elementText(findById("p.l1")) mustBe messages("declarationNotFound.li1")
    elementText(findById("p.l2")) mustBe messages("declarationNotFound.li2")
    elementText(findById("p.l3")) mustBe messages("declarationNotFound.li3")
    elementText(findById("p.l4")) mustBe "You can try again or make a new declaration."
  }

  "redirects to correct pages when clicking link" in {
    givenAJourneyWithSession()
    goToDeclarationNotFoundPage

    submitPage(DeclarationNotFoundPage, "tryAgain")
    pageTitle mustBe RetrieveDeclarationPage.title

    goToDeclarationNotFoundPage
    submitPage(DeclarationNotFoundPage, "makeNewDeclaration")
    pageTitle mustBe StartImportPage.title
  }
}

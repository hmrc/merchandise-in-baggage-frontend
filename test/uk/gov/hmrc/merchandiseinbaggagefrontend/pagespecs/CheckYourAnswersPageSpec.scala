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

import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.CheckYourAnswersPage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CheckYourAnswersPage, InvalidRequestPage}
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.PayApiStub._

class CheckYourAnswersPageSpec extends BasePageSpec[CheckYourAnswersPage] with TaxCalculation{
  override lazy val page: CheckYourAnswersPage = checkYourAnswersPage

  "the page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)

    "render correctly" when {
      "the declaration is complete" in {
        val taxDue = givenADeclarationWithTaxDue(completedDeclarationJourney).futureValue
        val declaration = completedDeclarationJourney.declarationIfRequiredAndComplete.get

        open(path)

        checkYourAnswersPage.mustRenderBasicContent(path, title)
        checkYourAnswersPage.mustRenderDetail(declaration, taxDue.totalTaxDue)
      }

      "the declaration is complete but sparse" in {
        val declaration = sparseCompleteDeclarationJourney.declarationIfRequiredAndComplete.get
        val taxDue = givenADeclarationWithTaxDue(sparseCompleteDeclarationJourney).futureValue

        open(path)

        page.mustRenderBasicContent(path, title)
        page.mustRenderDetail(declaration, taxDue.totalTaxDue)
      }
    }

    s"redirect to ${InvalidRequestPage.path}" when {
      "the declaration journey is not complete" in {
        givenADeclarationJourney(incompleteDeclarationJourney)

        open(path) mustBe InvalidRequestPage.path
      }
    }

    "allow the user to make a payment" in {
      givenADeclarationWithTaxDue(completedDeclarationJourney).futureValue
      givenTaxArePaid(wireMockServer)

      open(path)

      page.mustRedirectToPaymentFromTheCTA()
      page.mustHaveOneRequestAndSessionId(wireMockServer)
    }
  }
}

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

import com.github.tomakehurst.wiremock.WireMockServer
import com.softwaremill.macwire.wire
import org.scalatest.Assertion
import uk.gov.hmrc.http.HeaderNames.{xRequestId, xSessionId}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationType
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.CheckYourAnswersPage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CheckYourAnswersPage, InvalidRequestPage}
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.PayApiStub._

import scala.collection.JavaConverters._

class CheckYourAnswersPageSpec extends BasePageSpec[CheckYourAnswersPage] with TaxCalculation{
  override lazy val page: CheckYourAnswersPage = wire[CheckYourAnswersPage]

  "the page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aPageWithNoBackButton(path)

    "render correctly" when {
      "the declaration is complete" in {
        val taxDue = givenADeclarationWithTaxDue(completedDeclarationJourney).futureValue
        val declaration = completedDeclarationJourney.declarationIfRequiredAndComplete.get

        open(path)

        page.headerText() mustBe title
        page.mustRenderDetail(declaration, taxDue.totalTaxDue)
      }

      "the declaration is complete but sparse" in {
        val declaration = sparseCompleteDeclarationJourney.declarationIfRequiredAndComplete.get
        val taxDue = givenADeclarationWithTaxDue(sparseCompleteDeclarationJourney).futureValue

        open(path)

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
      mustHaveOneRequestAndSessionId(wireMockServer)
    }

    "allow the user to make a declaration if exporting" in {
      givenADeclarationWithTaxDue(completedDeclarationJourney.copy(declarationType = DeclarationType.Export)).futureValue
      givenTaxArePaid(wireMockServer)

      open(path)

      page.mustRedirectToDeclarationConfirmation()
    }
  }

  def mustHaveOneRequestAndSessionId(server: WireMockServer): Assertion = {
    val payApiRequestCapture = server.getAllServeEvents.asScala
      .find(_.getRequest.getAbsoluteUrl.contains("pay-api/mib-frontend/mib/journey/start"))
      .get.getRequest

    payApiRequestCapture.header(xSessionId).values.size mustBe 1
    payApiRequestCapture.header(xRequestId).values.size mustBe 1
  }

  def mustRedirectToInvalidRequest(): Assertion =
    readPath() mustBe "/merchandise-in-baggage/invalid-request"
}

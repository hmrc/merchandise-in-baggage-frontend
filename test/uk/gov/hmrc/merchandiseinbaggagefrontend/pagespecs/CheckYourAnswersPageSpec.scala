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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, PaymentCalculations}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CheckYourAnswersPage, InvalidRequestPage}
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.CurrencyConversionStub.givenCurrencyIsFound

import scala.concurrent.Future

class CheckYourAnswersPageSpec extends BasePageSpec[CheckYourAnswersPage] {
  override lazy val page: CheckYourAnswersPage = checkYourAnswersPage

  private lazy val calculationService = injector.instanceOf[CalculationService]

  private val expectedTitle = "Check your answers before making your declaration"

  override def beforeEach(): Unit = {
    super.beforeEach()
    webDriver.manage().deleteAllCookies()
  }

  def createDeclarationAndCalculateTaxDue(declarationJourney: DeclarationJourney): Future[PaymentCalculations] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    givenCurrencyIsFound("EUR", wireMockServer)

    givenADeclarationJourney(declarationJourney)

    calculationService.paymentCalculation(declarationJourney.declarationIfRequiredAndComplete.get.declarationGoods)
  }

  "the page" should {
    behave like aPageWhichRequiresADeclarationJourney()

    "render correctly" when {
      "the declaration is complete" in {
        val taxDue = createDeclarationAndCalculateTaxDue(completedDeclarationJourney).futureValue
        val declaration = completedDeclarationJourney.declarationIfRequiredAndComplete.get

        checkYourAnswersPage.open()
        checkYourAnswersPage.mustRenderBasicContent(expectedTitle)

        checkYourAnswersPage.mustRenderDetail(declaration, taxDue.totalTaxDue)
      }

      "the declaration is complete but sparse" in {
        val declaration = sparseCompleteDeclarationJourney.declarationIfRequiredAndComplete.get
        val taxDue = createDeclarationAndCalculateTaxDue(sparseCompleteDeclarationJourney).futureValue

        page.open()
        page.mustRenderBasicContent(expectedTitle)

        page.mustRenderDetail(declaration, taxDue.totalTaxDue)
      }
    }

    s"redirect to ${InvalidRequestPage.path}" when {
      "the declaration journey is not complete" in {
        givenADeclarationJourney(incompleteDeclarationJourney)

        page.open() mustBe InvalidRequestPage.path
      }
    }

    "allow the user to make a payment" in {
      createDeclarationAndCalculateTaxDue(completedDeclarationJourney).futureValue

      page.open()

      page.mustRedirectToPaymentFromTheCTA()
    }
  }
}

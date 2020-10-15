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
import uk.gov.hmrc.merchandiseinbaggagefrontend.CoreTestData
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.TaxCalculations
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.CurrencyConversionStub.givenCurrencyIsFound

import scala.concurrent.Future

class CheckYourAnswersPageSpec extends BasePageSpec with CoreTestData {
  private val calculationService = injector.instanceOf[CalculationService]

  private def createDeclaration(): Future[TaxCalculations] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    givenCurrencyIsFound("EUR", wireMockServer)

    testOnlyDeclarationJourneyPage.open()
    testOnlyDeclarationJourneyPage.clickOnSubmitButton()

    calculationService.taxCalculation(declaration.declarationGoods)
  }

  "the page" should {
    "render correctly" in {
      val taxDue = createDeclaration().futureValue

      checkYourAnswersPage.open()
      checkYourAnswersPage.assertPageIsDisplayed()
      checkYourAnswersPage.assertDetailIsRendered(declaration, taxDue.totalTaxDue)
    }

    "allow the user to make a payment" in {
      createDeclaration().futureValue

      checkYourAnswersPage.open()
      checkYourAnswersPage.assertClickOnPayButtonRedirectsToPayFrontend()
    }
  }
}

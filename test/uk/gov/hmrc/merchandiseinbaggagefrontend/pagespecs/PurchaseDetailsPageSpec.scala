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

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, PurchaseDetailsInput}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{InvoiceNumberPage, PurchaseDetailsPage}
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.CurrencyConversionStub.givenCurrenciesAreFound

class PurchaseDetailsPageSpec extends DeclarationDataCapturePageSpec[PurchaseDetailsInput, PurchaseDetailsPage] with ScalaFutures {
  override lazy val page: PurchaseDetailsPage = purchaseDetailsPage

  private val expectedTitle = "How much did you pay for the test good?"

  def givenJourneyAndCurrenciesFound(journey: DeclarationJourney) = {
    givenCurrenciesAreFound(wireMockServer)

    givenADeclarationJourney(journey)
  }

  "the purchase details page" should {
    behave like aPageWhichRenders(givenJourneyAndCurrenciesFound(declarationJourneyWithStartedGoodsEntry), expectedTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(givenJourneyAndCurrenciesFound(completedDeclarationJourney))
    behave like aPageWhichRequiresADeclarationJourney()
    behave like aDataCapturePageWithSimpleRouting(givenJourneyAndCurrenciesFound(declarationJourneyWithStartedGoodsEntry), Seq(PurchaseDetailsInput("100", "EUR")), InvoiceNumberPage.path())
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[PurchaseDetailsInput] =
    declarationJourney.goodsEntries.entries.head.maybePurchaseDetails map (_.purchaseDetailsInput)
}

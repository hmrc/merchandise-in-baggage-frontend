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
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.InvoiceNumberPage
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.InvoiceNumberPage._

class InvoiceNumberPageSpec extends DeclarationDataCapturePageSpec[String, InvoiceNumberPage] with ScalaFutures {
  override lazy val page: InvoiceNumberPage = invoiceNumberPage

  "the invoice number page" should {
    val path = InvoiceNumberPage.path()
    behave like aPageWhichRenders(path, givenADeclarationJourney(declarationJourneyWithStartedGoodsEntry),title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWhichRequiresADeclarationJourney(path)
    //behave like aDataCapturePageWithSimpleRouting(givenADeclarationJourney(declarationJourneyWithStartedGoodsEntry), Seq("123456789"), ReviewGoodsPage.path())
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[String] =
    declarationJourney.goodsEntries.entries.head.maybeInvoiceNumber
}

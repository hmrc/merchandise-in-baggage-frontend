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

import org.openqa.selenium.By
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResult, CalculationResults}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, NotRequired}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.DeclarationConfirmationPage
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenPersistedDeclarationIsFound
import uk.gov.hmrc.merchandiseinbaggage.utils.DateUtils.LocalDateTimeOps
import scala.collection.JavaConverters._

class DeclarationConfirmationContentSpec extends DeclarationConfirmationPage with CoreTestData {

  "it should show the confirmation content as expected for exports" in {
    val journey = givenAJourneyWithSession()
    givenPersistedDeclarationIsFound(declaration.copy(declarationType = Export), journey.declarationId)
    goToConfirmationPage

    findById("serviceLabel").getText mustBe "Declaration"
    findById("service").getText mustBe "Commercial goods carried in accompanied baggage or small vehicles"

    findById("dateOfDeclarationLabel").getText mustBe "Date"
    findById("dateOfDeclaration").getText mustBe declarationWithPaidAmendment.dateOfDeclaration.formattedDateNoTime

    findById("amountLabel").getText mustBe "Amount"
    findById("amount").getText mustBe "Nothing to pay"

    findById("printDeclarationId").getText mustBe "Print or save a copy of this page"

    findById("whatToDoNextId").getText mustBe "What you need to do next"

    val bulletPoints = findBullets()

    bulletPoints.size mustBe 2
    elementText(bulletPoints.head) mustBe s"${messages("declarationConfirmation.ul.2")}"
    elementText(bulletPoints(1)) mustBe s"${messages("declarationConfirmation.ul.3")}"

    findById("makeAnotherDeclarationId").getText mustBe "Make a new declaration"
    findById("makeAnotherDeclarationId").getAttribute("href") mustBe fullUrl("/declare-commercial-goods/make-another-declaration")
    findById("changeDeclarationId").getText mustBe "Add goods to an existing declaration"
    findById("changeDeclarationId").getAttribute("href") mustBe fullUrl("/declare-commercial-goods/add-goods-to-an-existing-declaration")
  }

  "it should show the confirmation content as expected for Imports" in {
    val journey = givenAJourneyWithSession()
    val calcResult =
      CalculationResult(aImportGoods, AmountInPence(111000L), AmountInPence(5), AmountInPence(7), Some(aConversionRatePeriod))
    givenPersistedDeclarationIsFound(
      declaration.copy(
        paymentStatus = Some(NotRequired),
        maybeTotalCalculationResult = Some(aTotalCalculationResult.copy(calculationResults = CalculationResults(Seq(calcResult))))
      ),
      journey.declarationId
    )

    goToConfirmationPage

    findById("serviceLabel").getText mustBe "Declaration"
    findById("service").getText mustBe "Commercial goods carried in accompanied baggage or small vehicles"

    findById("dateOfDeclarationLabel").getText mustBe "Date"
    findById("dateOfDeclaration").getText mustBe declarationWithPaidAmendment.dateOfDeclaration.formattedDateNoTime

    findById("amountLabel").getText mustBe "Amount"
    findById("amount").getText mustBe "Nothing to pay"

    findById("printDeclarationId").getText mustBe "Print or save a copy of this page"

    findById("whatToDoNextId").getText mustBe "What you need to do next"

    val bulletPoints = findBullets()

    bulletPoints.size mustBe 4
    elementText(bulletPoints.head) mustBe "go through the green channel (nothing to declare) at customs"
    elementText(bulletPoints(1)) mustBe "take the declaration sent to the email provided"
    elementText(bulletPoints(2)) mustBe "take the receipts or invoices for all the declared goods"
    elementText(bulletPoints(3)) mustBe "take proof which shows where the EU goods were produced"

    findById("bringingEUGoodsId").getText mustBe "Bringing EU goods"

    findById("makeAnotherDeclarationId").getText mustBe "Make a new declaration"
    findById("makeAnotherDeclarationId").getAttribute("href") mustBe fullUrl("/declare-commercial-goods/make-another-declaration")
    findById("changeDeclarationId").getText mustBe "Add goods to an existing declaration"
    findById("changeDeclarationId").getAttribute("href") mustBe fullUrl("/declare-commercial-goods/add-goods-to-an-existing-declaration")
  }

  private def findBullets() =
    findByXPath("//ul[@id='whatToDoNextUlId']")
      .findElements(By.tagName("li"))
      .asScala
      .toList
}

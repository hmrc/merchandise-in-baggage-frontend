/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation._
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class MibConnectorSpec extends BaseSpecWithApplication with CoreTestData {

  private val client             = app.injector.instanceOf[MibConnector]
  private val stub               = app.injector.instanceOf[MibBackendStub]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val declarationWithId  = declaration.copy(declarationId = stub.stubbedDeclarationId)

  "send a declaration to backend to be persisted" in {
    stub.givenDeclarationIsPersistedInBackend(declarationWithId)

    client.persistDeclaration(declarationWithId).futureValue mustBe stub.stubbedDeclarationId
  }

  "send a calculation request to backend for payment" in {
    val calculationRequest = List(aGoods).map(_.calculationRequest(GreatBritain))
    val stubbedResult      =
      List(CalculationResult(aGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), None))

    stub.givenAPaymentCalculations(calculationRequest, stubbedResult)

    client.calculatePayments(calculationRequest).futureValue mustBe CalculationResponse(
      CalculationResults(stubbedResult),
      WithinThreshold
    )
  }

  "send a calculation requests to backend for amend payment" in {
    val amend          = Amendment(111, LocalDateTime.now.truncatedTo(ChronoUnit.MILLIS), DeclarationGoods(Seq(aImportGoods)))
    val amendRequest   = CalculationAmendRequest(Some(amend), Some(GreatBritain), aDeclarationId)
    val stubbedResults =
      CalculationResult(aGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), None) :: Nil

    stub.givenAnAmendPaymentCalculationsRequest(amendRequest, stubbedResults)

    client.calculatePaymentsAmendPlusExisting(amendRequest).futureValue mustBe CalculationResponse(
      CalculationResults(stubbedResults),
      WithinThreshold
    )
  }

  "find a persisted declaration from backend by declarationId" in {
    stub.givenPersistedDeclarationIsFound(declarationWithId, stub.stubbedDeclarationId)

    client.findDeclaration(stub.stubbedDeclarationId).futureValue mustBe Some(declarationWithId)
  }

  "check eori number" in {
    stub.givenEoriIsChecked(aEoriNumber)

    client.checkEoriNumber(aEoriNumber).futureValue mustBe aCheckResponse
  }

  "findBy query" should {
    "return declarationId as expected" in {
      stub.givenFindByDeclarationReturnSuccess(mibReference, eori, declaration)
      client.findBy(mibReference, eori).value.futureValue mustBe Right(Some(declaration))
    }

    "handle 404 from BE" in {
      stub.givenFindByDeclarationReturnStatus(mibReference, eori, 404)
      client.findBy(mibReference, eori).value.futureValue mustBe Right(None)
    }

    "handle unexpected error from BE" in {
      stub.givenFindByDeclarationReturnStatus(mibReference, eori, 500)
      client.findBy(mibReference, eori).value.futureValue.isLeft mustBe true
    }
  }
}

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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResult, CalculationResults, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

class MibConnectorSpec extends BaseSpecWithApplication with CoreTestData with WireMockSupport {

  private val client = app.injector.instanceOf[MibConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val declarationWithId = declaration.copy(declarationId = stubbedDeclarationId)

  "send a declaration to backend to be persisted" in {
    givenDeclarationIsPersistedInBackend(declarationWithId)

    client.persistDeclaration(declarationWithId).futureValue mustBe stubbedDeclarationId
  }

  "send a calculation request to backend for payment" in {
    val calculationRequest = List(aGoods).map(_.calculationRequest(GreatBritain))
    val stubbedResult = List(CalculationResult(aGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), None))

    givenAPaymentCalculations(calculationRequest, stubbedResult)

    client.calculatePayments(calculationRequest).futureValue mustBe CalculationResults(stubbedResult, WithinThreshold)
  }

  "send a calculation requests to backend for payment" in {
    val calculationRequests = aDeclarationGood.goods.map(_.calculationRequest(GreatBritain))
    val stubbedResults =
      CalculationResult(aGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), None) :: Nil

    givenAPaymentCalculations(calculationRequests, stubbedResults)

    client.calculatePayments(calculationRequests).futureValue mustBe CalculationResults(stubbedResults, WithinThreshold)
  }

  "find a persisted declaration from backend by declarationId" in {
    givenPersistedDeclarationIsFound(declarationWithId, stubbedDeclarationId)

    client.findDeclaration(stubbedDeclarationId).futureValue mustBe Some(declarationWithId)
  }

  "check eori number" in {
    givenEoriIsChecked(aEoriNumber)

    client.checkEoriNumber(aEoriNumber).futureValue mustBe aCheckResponse
  }

  "findBy query" should {
    "return declarationId as expected" in {
      givenFindByDeclarationReturnSuccess(mibReference, eori, declaration)
      client.findBy(mibReference, eori).value.futureValue mustBe Right(Some(declaration))
    }

    "handle 404 from BE" in {
      givenFindByDeclarationReturnStatus(mibReference, eori, 404)
      client.findBy(mibReference, eori).value.futureValue mustBe Right(None)
    }

    "handle unexpected error from BE" in {
      givenFindByDeclarationReturnStatus(mibReference, eori, 500)
      client.findBy(mibReference, eori).value.futureValue.isLeft mustBe true
    }
  }
}

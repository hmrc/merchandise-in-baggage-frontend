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

package uk.gov.hmrc.merchandiseinbaggage.service

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{JourneyId, PayApiResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.api.tpspayments.TpsPaymentsRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.URL
import uk.gov.hmrc.merchandiseinbaggage.stubs.TpsPaymentsBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

class TpsPaymentsServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {

  val paymentService = app.injector.instanceOf[TpsPaymentsService]

  "makes the payment to TPS" in {
    val payApiResponse = PayApiResponse(JourneyId("123"), URL("url"))

    givenTaxArePaid(payApiResponse)

    val actual =
      paymentService
        .createTpsPayments(None, completedDeclarationJourney.toDeclaration, aCalculationResults)
        .futureValue

    actual mustBe payApiResponse
  }

  "build a TpsPaymentsRequest from a declaration" in {
    val amendRef = 123
    val actual   =
      paymentService.buildTpsRequest(Some(amendRef), completedDeclarationJourney.toDeclaration, aCalculationResults)

    actual mustBe a[TpsPaymentsRequest]
    actual.amendmentReference mustBe Some(amendRef)
    actual.mibReference mustBe "xx"
    actual.customerName mustBe "Terry Test"
    actual.amount mustBe aCalculationResults.totalTaxDue.inPounds
    actual.totalVatDue mustBe aCalculationResults.totalVatDue.inPounds
    actual.totalDutyDue mustBe aCalculationResults.totalDutyDue.inPounds
  }
}

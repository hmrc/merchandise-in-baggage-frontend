/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.model.tpspayments.{TpsId, TpsPaymentsRequest}
import uk.gov.hmrc.merchandiseinbaggage.stubs.TpsPaymentsBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

class TpsPaymentsServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {

  val paymentService = app.injector.instanceOf[TpsPaymentsService]

  "makes the payment to TPS" in {
    val tpsId = TpsId("123")

    givenTaxArePaid(tpsId)

    val actual =
      paymentService
        .createTpsPayments(tpsId.value, None, completedDeclarationJourney.toDeclaration, aCalculationResults)
        .futureValue

    actual mustBe tpsId
  }

  "build a TpsPaymentsRequest from a declaration" in {
    val actual   =
      paymentService.buildTpsRequest("pid", Some(123), completedDeclarationJourney.toDeclaration, aCalculationResults)
    val payments = actual.payments.head

    actual mustBe a[TpsPaymentsRequest]
    actual.pid mustBe "pid"
    payments.paymentSpecificData.amendmentReference mustBe Some(123)
    payments.chargeReference mustBe "xx"
    payments.customerName mustBe "Terry Test"
    payments.paymentSpecificData.chargeReference mustBe "xx"
    payments.amount mustBe aCalculationResults.totalTaxDue.inPounds
    payments.paymentSpecificData.vat mustBe aCalculationResults.totalVatDue.inPounds
    payments.paymentSpecificData.customs mustBe aCalculationResults.totalDutyDue.inPounds
  }
}

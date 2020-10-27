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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.api

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpec, CoreTestData}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{AmountInPence, DeclarationGoods, PaymentCalculations}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class PayApiRequestBuilderSpec extends BaseSpec with CoreTestData with ScalaFutures {

  "Build a pay api request" in new PayApiRequestBuilder {
    import aCalculationResult._
    val stubbedReference = super.mibReference
    override def mibReference: Try[MibReference] = stubbedReference
    val stubbedService: DeclarationGoods => Future[PaymentCalculations] = _ => Future.successful(aPaymentCalculations)
    val totalDue: Long = duty.value + vat.value

    val expected: PayApiRequest = PayApiRequest(stubbedReference.get, AmountInPence(totalDue), duty, vat)

    buildRequest(aDeclarationGood, stubbedService).futureValue mustBe expected
  }
}

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

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{AmountInPence, DeclarationGoods, PaymentCalculations}
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.MibReferenceGenerator
import uk.gov.hmrc.merchandiseinbaggagefrontend.utils.ValueClassFormat

import scala.concurrent.{ExecutionContext, Future}


case class MibReference(value: String)
object MibReference {
  implicit val format: Format[MibReference] = ValueClassFormat.format(value => MibReference.apply(value))(_.value)
}

case class PayApiRequest(mibReference: MibReference, amountInPence: AmountInPence, vatAmountInPence: AmountInPence,
                         dutyAmountInPence: AmountInPence)

object PayApiRequest {
  implicit val format: Format[PayApiRequest] = Json.format[PayApiRequest]
}

trait PayApiRequestBuilder extends MibReferenceGenerator {
  def buildRequest(declarationGoods: DeclarationGoods, paymentCalculations: DeclarationGoods => Future[PaymentCalculations])
                  (implicit ec: ExecutionContext): Future[PayApiRequest] =
    for {
      taxDue    <- paymentCalculations(declarationGoods)
      reference <- Future.fromTry(mibReference)
    } yield PayApiRequest(reference, taxDue.totalTaxDue, taxDue.totalDutyDue, taxDue.totalVatDue)
}

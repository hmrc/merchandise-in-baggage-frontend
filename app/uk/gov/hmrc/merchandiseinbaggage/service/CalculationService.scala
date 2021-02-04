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

package uk.gov.hmrc.merchandiseinbaggage.service

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationGoods, ImportGoods, PaymentCalculation, PaymentCalculations}

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

@Singleton
class CalculationService @Inject()(mibConnector: MibConnector)(implicit ec: ExecutionContext) {
  private val logger = Logger("CalculationService")

  def paymentCalculation(declarationGoods: DeclarationGoods)(implicit hc: HeaderCarrier): Future[PaymentCalculations] =
    Future
      .traverse(declarationGoods.goods.asInstanceOf[Seq[ImportGoods]]) { goods =>
        mibConnector.calculatePayment(goods.calculationRequest).map { result =>
          logger.info(s"Payment calculation for good [$goods] gave result [$result]")
          PaymentCalculation(goods, result)
        }
      }
      .map(PaymentCalculations.apply)
}

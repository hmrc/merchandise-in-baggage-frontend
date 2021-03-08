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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, DeclarationId, DeclarationType, Goods, JourneyDetails}
import uk.gov.hmrc.merchandiseinbaggage.service.PreviousDeclarationDetailsService._

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousDeclarationDetailsService @Inject()(mibConnector: MibConnector) {

  def findDeclaration(declarationId: DeclarationId)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Option[(Seq[Goods], JourneyDetails, DeclarationType, Boolean)]] =
    mibConnector.findDeclaration(declarationId).map {
      case Some(declaration) =>
        Some(
          (
            listGoods(declaration.declarationGoods.goods, declaration.amendments),
            declaration.journeyDetails,
            declaration.declarationType,
            withinTimerange(declaration.journeyDetails.dateOfTravel, LocalDate.now)))
      case _ => None

    }

}

object PreviousDeclarationDetailsService {
  def listGoods(goods: Seq[Goods], amendments: Seq[Amendment]): Seq[Goods] = {
    def paidAmendment: Seq[Goods] = amendments.filterNot(_.paymentStatus.isEmpty) flatMap (a => a.goods.goods)

    goods ++ paidAmendment
  }

  def withinTimerange(travelDate: LocalDate, now: LocalDate): Boolean = {
    val startOfRange = travelDate.minusDays(5)
    val endOfRange = travelDate.plusDays(30)

    (now.isAfter(startOfRange) || now.isEqual(startOfRange)) &&
    (now.isBefore(endOfRange) || now.isEqual(endOfRange))
  }

}

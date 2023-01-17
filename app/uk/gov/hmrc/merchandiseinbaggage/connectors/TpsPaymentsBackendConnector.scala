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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import play.api.http.Status
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.tpspayments.{TpsId, TpsPaymentsRequest}
import javax.inject.{Inject, Named, Singleton}

import scala.concurrent.{ExecutionContext, Future}

case class TpsPaymentsException(message: String) extends RuntimeException(message)

@Singleton
class TpsPaymentsBackendConnector @Inject()(httpClient: HttpClient, @Named("tpsBackendBaseUrl") baseUrl: String) {

  def tpsPayments(requestBody: TpsPaymentsRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[TpsId] =
    httpClient.POST[TpsPaymentsRequest, HttpResponse](s"$baseUrl/tps-payments-backend/tps-payments", requestBody).map { response =>
      response.status match {
        case Status.CREATED => response.json.as[TpsId]
        case other: Int =>
          throw TpsPaymentsException(
            s"unexpected status from tps-payments-backend for reference: ${requestBody.payments.head.chargeReference}, status: $other")
      }
    }
}

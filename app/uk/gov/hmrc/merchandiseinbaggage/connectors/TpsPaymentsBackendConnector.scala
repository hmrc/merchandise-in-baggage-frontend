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

import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits.*
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.PayApiResponse
import uk.gov.hmrc.merchandiseinbaggage.model.api.tpspayments.TpsPaymentsRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsPaymentsBackendConnector @Inject() (appConfig: AppConfig, httpClient: HttpClientV2) {

  private val baseUrl = appConfig.tpsPaymentsBackendUrl
  private val fullUrl = s"$baseUrl/tps-payments-backend/start-tps-journey/mib"

  def tpsPayments(
    requestBody: TpsPaymentsRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PayApiResponse] =
    httpClient
      .post(url"$fullUrl")
      .withBody(Json.toJson(requestBody))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case Status.CREATED => response.json.as[PayApiResponse]
          case other: Int     =>
            throw new RuntimeException(
              s"unexpected status from tps-payments-backend for reference: ${requestBody.mibReference}, status: $other"
            )
        }
      }
}

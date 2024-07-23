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

import play.api.Logging
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.{PayApiRequest, PayApiResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentConnector @Inject() (appConfig: AppConfig, httpClient: HttpClientV2) extends Logging {

  private val url = url"${appConfig.paymentUrl}/pay-api/mib-frontend/mib/journey/start"

  def sendPaymentRequest(
    requestBody: PayApiRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PayApiResponse] = {
    logger.warn(s"payments requestBody: ${Json.toJson(requestBody)}")

    httpClient.post(url).withBody(Json.toJson(requestBody)).execute[HttpResponse].map { response =>
      response.status match {
        case Status.CREATED => response.json.as[PayApiResponse]
        case other: Int     =>
          throw new RuntimeException(
            s"unexpected status from pay-api for reference:${requestBody.mibReference.value}, status:$other"
          )
      }
    }
  }
}

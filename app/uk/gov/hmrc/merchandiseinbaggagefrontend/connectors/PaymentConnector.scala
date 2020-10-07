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

package uk.gov.hmrc.merchandiseinbaggagefrontend.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.PaymentServiceConf
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.{PayApiResponse, PayApitRequest}
import uk.gov.hmrc.merchandiseinbaggagefrontend.utils.SessionIdGenerator
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

import scala.concurrent.{ExecutionContext, Future}

trait PaymentConnector extends PaymentServiceConf with SessionIdGenerator {

  def makePayment(httpClient: HttpClient, requestBody: PayApitRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    httpClient.POST[PayApitRequest, HttpResponse](s"$paymentBaseUri${paymentServiceConf.url.value}", requestBody, addSessionId(hc).headers)
  }

  protected def extractUrl(response: HttpResponse): PayApiResponse =
    Json.parse(response.body).as[PayApiResponse]
}

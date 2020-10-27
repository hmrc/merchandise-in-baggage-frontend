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

import javax.inject.{Inject, Named, Singleton}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.{PayApiRequest, PayApiResponse}
import PaymentApiUrls._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentConnector @Inject()(httpClient: HttpClient, @Named("paymentBaseUrl") baseUrl: String) {
  def makePayment(requestBody: PayApiRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    httpClient.POST[PayApiRequest, HttpResponse](
      s"$baseUrl$payUrl", requestBody)
  }

  def extractUrl(response: HttpResponse): PayApiResponse = Json.parse(response.body).as[PayApiResponse]
}

object PaymentApiUrls {
  val payUrl = "/pay-api/mib-frontend/mib/journey/start"
  val payInitiatedJourneyUrl = "/pay/initiate-journey"
}

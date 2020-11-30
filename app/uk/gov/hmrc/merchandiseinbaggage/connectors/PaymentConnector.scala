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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import javax.inject.{Inject, Named, Singleton}
import play.api.http.Status
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggage.connectors.PaymentApiUrls._
import uk.gov.hmrc.merchandiseinbaggage.model.api.{PayApiRequest, PayApiResponse}

import scala.concurrent.{ExecutionContext, Future}

case class PayApiException(message: String) extends RuntimeException(message)

@Singleton
class PaymentConnector @Inject()(httpClient: HttpClient, @Named("paymentBaseUrl") baseUrl: String) {

  private val url = s"$baseUrl$payUrl"

  def sendPaymentRequest(requestBody: PayApiRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PayApiResponse] = {
    httpClient.POST[PayApiRequest, HttpResponse](url, requestBody).map { response =>
      response.status match {
        case Status.CREATED => response.json.as[PayApiResponse]
        case other: Int =>
          //TODO: PagerDuty
          throw PayApiException(s"unexpected status from pay-api for reference:${requestBody.mibReference.value}, status:$other")
      }
    }
  }
}

object PaymentApiUrls {
  val payUrl = "/pay-api/mib-frontend/mib/journey/start"
  val payInitiatedJourneyUrl = "/pay/initiate-journey"
}

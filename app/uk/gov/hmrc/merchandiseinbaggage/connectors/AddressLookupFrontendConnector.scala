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

import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.merchandiseinbaggage.config.AddressLookupConfig._
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.Address

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupFrontendConnector @Inject() (appConfig: AppConfig, http: HttpClientV2) {

  private val baseUrl = appConfig.addressLookupFrontendUrl

  private lazy val initJourneyUrl           = url"$baseUrl/api/v2/init"
  private def confirmJourneyUrl(id: String) = url"$baseUrl/api/confirmed?id=$id"

  def initJourney(call: Call, isAssistedDigital: Boolean)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[String] = {
    val callback      = appConfig.addressLookupCallbackUrl(isAssistedDigital)
    val addressConfig = Json.toJson(configAddressLookup(s"$callback${call.url}"))

    http.post(initJourneyUrl).withBody(addressConfig).execute[HttpResponse].map { response =>
      response.header(LOCATION).getOrElse {
        throw new RuntimeException("Response from AddressLookupFrontend did not contain LOCATION header.")
      }
    }
  }

  def getAddress(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Address] =
    http.get(confirmJourneyUrl(id)).execute[JsObject].map(json => (json \ "address").as[Address])

}

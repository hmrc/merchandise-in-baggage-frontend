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
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggage.config.AddressLookupConfig
import uk.gov.hmrc.merchandiseinbaggage.model.adresslookup.Address

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupFrontendConnector @Inject()(http: HttpClient,
                                              @Named("addressLookupFrontendBaseUrl") baseUrl: String,
                                              @Named("addressLookupCallback") callback: String,
                                              addressLookupConfig: AddressLookupConfig) {

  private lazy val initJourneyUrl = s"$baseUrl/api/v2/init"
  private def confirmJourneyUrl(id: String) = s"$baseUrl/api/confirmed?id=$id"

  def initJourney(call: Call)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {
    val addressConfig = Json.toJson(addressLookupConfig.config(s"$callback${call.url}"))

    http.POST[JsValue, HttpResponse](initJourneyUrl, addressConfig) map { response =>
      response.header(LOCATION).getOrElse {
        throw new RuntimeException("Response from AddressLookupFrontend did not contain LOCATION header.")
      }
    }
  }

  def getAddress(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Address] =
    http.GET[JsObject](confirmJourneyUrl(id)) map ( json => (json \ "address").as[Address])

}

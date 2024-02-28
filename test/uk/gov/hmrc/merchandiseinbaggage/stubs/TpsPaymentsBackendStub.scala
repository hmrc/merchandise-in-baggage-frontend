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

package uk.gov.hmrc.merchandiseinbaggage.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.PayApiResponse

object TpsPaymentsBackendStub extends CoreTestData {

  def givenTaxArePaid(tpsResponse: PayApiResponse)(implicit server: WireMockServer): StubMapping = {
    server.stubFor(
      post(urlPathEqualTo("/tps-payments-backend/start-tps-journey/mib"))
        .willReturn(okJson(Json.toJson(tpsResponse).toString).withStatus(CREATED))
    )
    server.stubFor(
      get(urlPathEqualTo("/tps-payments/make-payment/mib/123"))
        .willReturn(aResponse().withStatus(OK))
    )
  }

}

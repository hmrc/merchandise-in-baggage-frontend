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

package uk.gov.hmrc.merchandiseinbaggage.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.tpspayments.TpsId

object TpsPaymentsBackendStub extends CoreTestData {

  def givenTaxArePaid(tpsId: TpsId): StubMapping = {
    stubFor(
      post(urlPathEqualTo("/tps-payments-backend/tps-payments"))
        .willReturn(okJson(Json.toJson(tpsId).toString).withStatus(201)))
    stubFor(
      get(urlPathEqualTo("/tps-payments/make-payment/mib/123"))
        .willReturn(aResponse().withStatus(200)))
  }

}

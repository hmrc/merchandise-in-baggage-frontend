/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.http.Status
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData

object PayApiStub extends CoreTestData {

  val stubbedResponse = s"""{"journeyId":"5f3bc55","nextUrl":"http://localhost:17777/pay/initiate-journey"}"""

  def givenTaxArePaid(server: WireMockServer): StubMapping = {
    server
      .stubFor(
        post(urlPathEqualTo("/pay-api/mib-frontend/mib/journey/start"))
          .willReturn(okJson(stubbedResponse).withStatus(Status.CREATED))
      )
    server
      .stubFor(
        get(urlPathEqualTo("/pay/initiate-journey"))
          .willReturn(aResponse().withStatus(Status.OK))
      )
  }

}

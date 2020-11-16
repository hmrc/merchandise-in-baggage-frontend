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

package uk.gov.hmrc.merchandiseinbaggagefrontend.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{post, urlPathEqualTo, _}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.Declaration

object MibBackendStub extends MibConfiguration {

  def givenDeclarationIsPersistedInBackend(server: WireMockServer, declaration: Declaration): StubMapping = {
    server
      .stubFor(post(urlPathEqualTo(s"$declarationsUrl"))
        .withRequestBody(equalToJson(toJson(declaration).toString, true, false))
        .willReturn(created()))
  }

  def givenDeclarationIsPersistedInBackend(server: WireMockServer): StubMapping = {
    server
      .stubFor(post(urlPathEqualTo(s"$declarationsUrl"))
        .willReturn(created()))
  }
}

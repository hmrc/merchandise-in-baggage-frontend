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
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation._

object MibBackendStub extends CoreTestData {

  val stubbedDeclarationId: DeclarationId = DeclarationId("test-mib-be-id")

  def givenDeclarationIsPersistedInBackend(declaration: Declaration)(implicit server: WireMockServer): StubMapping =
    server
      .stubFor(
        post(urlPathEqualTo("/declare-commercial-goods/declarations"))
          .withRequestBody(equalToJson(toJson(declaration).toString, true, false))
          .willReturn(aResponse().withStatus(Status.CREATED).withBody(Json.toJson(declaration.declarationId).toString))
      )

  def givenDeclarationIsPersistedInBackend(implicit server: WireMockServer): StubMapping =
    server
      .stubFor(
        post(urlPathEqualTo("/declare-commercial-goods/declarations"))
          .willReturn(aResponse().withStatus(Status.CREATED).withBody(Json.toJson(stubbedDeclarationId).toString))
      )

  def givenDeclarationIsAmendedInBackend(implicit server: WireMockServer): StubMapping =
    server
      .stubFor(
        put(urlPathEqualTo("/declare-commercial-goods/declarations"))
          .willReturn(aResponse().withStatus(Status.OK).withBody(Json.toJson(stubbedDeclarationId).toString))
      )

  def givenPersistedDeclarationIsFound(declaration: Declaration, declarationId: DeclarationId = stubbedDeclarationId)(
    implicit server: WireMockServer
  ): StubMapping = {
    val declarationWithId = declaration.copy(declarationId = declarationId)
    server
      .stubFor(
        get(urlPathEqualTo(s"/declare-commercial-goods/declarations/${declarationWithId.declarationId.value}"))
          .willReturn(okJson(Json.toJson(declarationWithId).toString))
      )
  }

  def givenPersistedDeclarationIsFound()(implicit server: WireMockServer): StubMapping =
    server
      .stubFor(
        get(urlMatching(s"/declare-commercial-goods/declarations/(.*)"))
          .willReturn(
            okJson(
              Json.toJson(declaration.copy(declarationId = stubbedDeclarationId, declarationType = Export)).toString
            )
          )
      )

  def givenFindByDeclarationReturnSuccess(mibReference: MibReference, eori: Eori, declaration: Declaration)(implicit
    server: WireMockServer
  ): StubMapping =
    server
      .stubFor(
        get(urlEqualTo(s"/declare-commercial-goods/declarations?mibReference=${mibReference.value}&eori=${eori.value}"))
          .willReturn(okJson(Json.toJson(declaration).toString()))
      )

  def givenFindByDeclarationReturnStatus(mibReference: MibReference, eori: Eori, aStatus: Int)(implicit
    server: WireMockServer
  ): StubMapping =
    server
      .stubFor(
        get(urlEqualTo(s"/declare-commercial-goods/declarations?mibReference=${mibReference.value}&eori=${eori.value}"))
          .willReturn(status(aStatus))
      )

  def givenAPaymentCalculations(
    requests: Seq[CalculationRequest],
    results: Seq[CalculationResult],
    thresholdCheck: ThresholdCheck = WithinThreshold
  )(implicit server: WireMockServer): StubMapping =
    server
      .stubFor(
        post(urlPathEqualTo("/declare-commercial-goods/calculations"))
          .withRequestBody(equalToJson(toJson(requests).toString, true, false))
          .willReturn(okJson(Json.toJson(CalculationResponse(CalculationResults(results), thresholdCheck)).toString))
      )

  def givenAnAmendPaymentCalculationsRequest(
    request: CalculationAmendRequest,
    results: Seq[CalculationResult],
    thresholdCheck: ThresholdCheck = WithinThreshold
  )(implicit server: WireMockServer): StubMapping =
    server
      .stubFor(
        post(urlPathEqualTo("/declare-commercial-goods/amend-calculations"))
          .withRequestBody(equalToJson(toJson(request).toString, true, false))
          .willReturn(okJson(Json.toJson(CalculationResponse(CalculationResults(results), thresholdCheck)).toString))
      )

  def givenAnAmendPaymentCalculations(
    results: Seq[CalculationResult],
    thresholdCheck: ThresholdCheck = WithinThreshold
  )(implicit server: WireMockServer): StubMapping =
    server
      .stubFor(
        post(urlPathEqualTo("/declare-commercial-goods/amend-calculations"))
          .willReturn(okJson(Json.toJson(CalculationResponse(CalculationResults(results), thresholdCheck)).toString))
      )

  def givenAPaymentCalculation(result: CalculationResult, thresholdCheck: ThresholdCheck = WithinThreshold)(implicit
    server: WireMockServer
  ): StubMapping =
    server
      .stubFor(
        post(urlPathEqualTo("/declare-commercial-goods/calculations"))
          .willReturn(
            okJson(Json.toJson(CalculationResponse(CalculationResults(List(result)), thresholdCheck)).toString)
          )
      )

  def givenEoriIsChecked(eoriNumber: String)(implicit server: WireMockServer): StubMapping =
    server
      .stubFor(
        get(urlPathEqualTo(s"/declare-commercial-goods/validate/eori/$eoriNumber"))
          .willReturn(ok().withBody(Json.toJson(aCheckResponse).toString))
      )
}

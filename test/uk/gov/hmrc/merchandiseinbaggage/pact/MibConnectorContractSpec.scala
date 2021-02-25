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

package uk.gov.hmrc.merchandiseinbaggage.pact

import java.io.File

import com.itv.scalapact.ScalaPactForger._
import com.itv.scalapact.circe13._
import com.itv.scalapact.model.{ScalaPactDescription, ScalaPactOptions}
import org.json4s.DefaultFormats
import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationId}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MibConnectorContractSpec extends BaseSpecWithApplication with CoreTestData with MibConfiguration with WireMockSupport {

  implicit val formats: DefaultFormats.type = DefaultFormats

  val CONSUMER = "merchandise-in-baggage-frontend"
  val PROVIDER = "merchandise-in-baggage"
  val mibConnector = injector.instanceOf[MibConnector]

  val pact: ScalaPactDescription = forgePact
    .between(CONSUMER)
    .and(PROVIDER)
    .addInteraction(
      interaction
        .description("Persisting a declaration")
        .given("persistDeclarationTest")
        .uponReceiving(POST, s"$declarationsUrl", None, Map("Content-Type" -> "application/json"), Json.toJson(declaration).toString)
        .willRespondWith(201, Json.toJson(declaration.declarationId).toString)
    )
    .addInteraction(
      interaction
        .description("find a declaration")
        .given(s"id1234XXX${Json.toJson(declaration.copy(declarationId = DeclarationId("56789"))).toString}")
        .uponReceiving(GET, s"$declarationsUrl/56789")
        .willRespondWith(200, Json.toJson(declaration.copy(declarationId = DeclarationId("56789"))).toString)
    )
    .addInteraction(
      interaction
        .description("calculate payments")
        .given(s"calculatePaymentsTest")
        .uponReceiving(
          POST,
          s"$calculationsUrl",
          None,
          Map("Content-Type" -> "application/json"),
          Json.toJson(List(aImportGoods).map(_.calculationRequest)).toString)
        .willRespondWith(200, Json.toJson(List(aImportGoods).map(_.calculationRequest)).toString)
    )
    .addInteraction(
      interaction
        .description("check EoriNumber")
        .given(s"checkEoriNumberTest")
        .uponReceiving(GET, s"${checkEoriUrl}GB123")
        .willRespondWith(200, Json.toJson(CheckResponse("GB123", true, None)).toString)
    )

  implicit val options: ScalaPactOptions = ScalaPactOptions(true, "./pact")

  override def beforeAll(): Unit = {
    super.beforeAll()
    val pactDir = new File("./pact")
    if (pactDir.exists()) pactDir.listFiles().map(_.delete())
    pact.writePactsToFile
  }

  "Connecting to the merchandise-in-baggage BE Provider service" must {
    "be able to persist a declaration" in {
      givenDeclarationIsPersistedInBackend(declaration)
      val results: Future[DeclarationId] = mibConnector.persistDeclaration(declaration)
      results.futureValue mustBe declaration.declarationId
    }

    "be able to fetch a declaration" in {
      givenPersistedDeclarationIsFound(declaration)
      val results: Future[Option[Declaration]] = mibConnector.findDeclaration(stubbedDeclarationId)
      results.futureValue mustBe Some(declaration)
    }
  }
}

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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import java.time.LocalDateTime
import play.api.mvc.Result
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukHeader, GovukTemplate}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationId}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResult, CalculationResults}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.views.html.layouts.govukLayout
import uk.gov.hmrc.merchandiseinbaggage.views.html.{DeclarationConfirmationView, Head, Layout}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DeclarationConfirmationControllerSpec extends DeclarationJourneyControllerSpec with WireMockSupport with MibConfiguration {

  import mibConf._
  private val view = app.injector.instanceOf[DeclarationConfirmationView]
  private val client = app.injector.instanceOf[HttpClient]
  private val connector = new MibConnector(client, s"$protocol://$host:${WireMockSupport.port}")
  private val controller =
    new DeclarationConfirmationController(controllerComponents, actionBuilder, view, connector, declarationJourneyRepository)

  "on page load return 200 if declaration exists and resets the journey" in {
    val sessionId = SessionId()
    val id = DeclarationId("456")
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val request = buildGet(routes.DeclarationConfirmationController.onPageLoad().url, sessionId)

    val exportJourney: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId, declarationType = DeclarationType.Export, createdAt = created, declarationId = id)

    givenADeclarationJourneyIsPersisted(exportJourney)

    givenPersistedDeclarationIsFound(exportJourney.declarationIfRequiredAndComplete.get, id)

    val eventualResult = controller.onPageLoad()(request)
    status(eventualResult) mustBe 200

    import exportJourney._
    val resetJourney = DeclarationJourney(sessionId, declarationType)

    declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.sessionId mustBe resetJourney.sessionId
    declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.declarationType mustBe resetJourney.declarationType
  }

  private def generateDeclarationConfirmationPage(decType: DeclarationType, purchaseAmount: Long): String = {
    val dummyAmount = AmountInPence(0)

    def totalCalculationResult: TotalCalculationResult =
      TotalCalculationResult(
        CalculationResults(
          Seq(CalculationResult(
            ImportGoods(
              CategoryQuantityOfGoods("sock", "1"),
              GoodsVatRates.Twenty,
              YesNoDontKnow.Yes,
              PurchaseDetails(purchaseAmount.toString, Currency("GBP", "title.british_pounds_gbp", None, List.empty[String]))
            ),
            AmountInPence(purchaseAmount),
            dummyAmount,
            dummyAmount,
            None
          ))),
        AmountInPence(purchaseAmount),
        dummyAmount,
        dummyAmount,
        dummyAmount
      )

    val sessionId = SessionId()
    val id = DeclarationId(java.util.UUID.randomUUID().toString)
    val created = LocalDateTime.now.withSecond(0).withNano(0)

    val journey: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId, declarationType = decType, createdAt = created, declarationId = id)

    val persistedDeclaration = journey.declarationIfRequiredAndComplete.map { x =>
      if (decType == DeclarationType.Import) x.copy(maybeTotalCalculationResult = Some(totalCalculationResult)) else x
    }

    val layout = app.injector.instanceOf[Layout]
    val link = app.injector.instanceOf[uk.gov.hmrc.merchandiseinbaggage.views.html.components.link]

    val d = new DeclarationConfirmationView(layout, null, link)

    implicit val r: play.api.mvc.Request[_] = fakeRequest
    val x: HtmlFormat.Appendable = d.apply(persistedDeclaration.get)

    x.body
  }

  "Import with value over 1000gbp, add an 'take proof' line" in {
    val result = generateDeclarationConfirmationPage(DeclarationType.Import, 111111)

    result must include(messageApi("declarationConfirmation.ul.3"))
  }

  "Import with value under 1000gbp, DONOT add an 'take proof' line" in {
    val result = generateDeclarationConfirmationPage(DeclarationType.Import, 100)

    result mustNot include(messageApi("declarationConfirmation.ul.3"))
  }

  "Export with value over 1000gbp, DONOT add an 'take proof' line" in {
    val result = generateDeclarationConfirmationPage(DeclarationType.Export, 111111)

    result mustNot include(messageApi("declarationConfirmation.ul.3"))
  }

  "on page load return an invalid request if journey is invalidated by resetting" in {
    val connector = new MibConnector(client, "") {
      override def findDeclaration(
        declarationId: DeclarationId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Declaration]] =
        Future.failed(new Exception("not found"))
    }

    val controller =
      new DeclarationConfirmationController(controllerComponents, actionBuilder, view, connector, declarationJourneyRepository)
    val request = buildGet(routes.DeclarationConfirmationController.onPageLoad().url, aSessionId)

    val eventualResult = controller.onPageLoad()(request)
    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some("/declare-commercial-goods/cannot-access-page")
  }

}

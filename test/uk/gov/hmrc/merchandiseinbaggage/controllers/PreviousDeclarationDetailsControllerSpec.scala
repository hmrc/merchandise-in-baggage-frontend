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

import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, CategoryQuantityOfGoods, Country, Currency, DeclarationGoods, DeclarationId, DeclarationType, ExportGoods, GoodsVatRates, ImportGoods, JourneyDetails, JourneyInSmallVehicle, NotRequired, Paid, PaymentStatus, Port, PurchaseDetails, SessionId, YesNoDontKnow}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenPersistedDeclarationIsFound
import uk.gov.hmrc.merchandiseinbaggage.views.html.{PreviousDeclarationDetailsView}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import scala.concurrent.ExecutionContext.Implicits.global

import java.time.{LocalDate, LocalDateTime}

class PreviousDeclarationDetailsControllerSpec extends DeclarationJourneyControllerSpec with WireMockSupport with MibConfiguration {

  val journeyDetails: JourneyDetails = JourneyInSmallVehicle(
    Port("DVR", "title.dover", isGB = true, List("Port of Dover")),
    LocalDate.now(),
    "T5 RRY"
  )

  def createImport(desc: String, price: String): ImportGoods = ImportGoods(
    CategoryQuantityOfGoods(desc, "1"),
    GoodsVatRates.Twenty,
    YesNoDontKnow.Yes,
    PurchaseDetails(price, Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European")))
  )

  def createExport(desc: String, price: String): ExportGoods = ExportGoods(
    CategoryQuantityOfGoods(desc, "1"),
    Country("FR", "title.france", "FR", isEu = true, Nil),
    PurchaseDetails(price, Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European")))
  )

  def createAmendment(desc: String, paymentStatus: Option[PaymentStatus]): Amendment = Amendment(
    LocalDateTime.now,
    DeclarationGoods(createImport(desc, "99.99") :: Nil),
    None,
    paymentStatus,
    Some("Digital")
  )

  "check the view can" should {
    "display 'continue' button when within date range" in {
      val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
      val goods = createImport("wine", "99.99") :: createImport("cheese", "199.99") :: Nil
      val result = view.render(goods, journeyDetails, DeclarationType.Import, withinTimerange = true, fakeRequest, messages, appConfig)
      val html = contentAsString(result)

      html must include(messageApi("previousDeclarationDetails.add_goods"))
      html must include("govuk-button")
    }

    "be 'to late' message" in {
      val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
      val goods = createImport("wine", "99.99") :: createImport("cheese", "199.99") :: Nil
      val result = view.render(goods, journeyDetails, DeclarationType.Import, withinTimerange = false, fakeRequest, messages, appConfig)
      val html = contentAsString(result)

      html must include(messageApi("previousDeclarationDetails.expired"))
      html mustNot include("govuk-button")
    }

    "list all the goods given" in {
      val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
      val itemCount = 5
      val goods = List.fill(itemCount)(createImport("wine", "99.99"))
      val result = view.render(goods, journeyDetails, DeclarationType.Import, withinTimerange = true, fakeRequest, messages, appConfig)
      val html = contentAsString(result)

      html must include(s"categoryLabel_${itemCount - 1}")
    }
  }

  "Declaration time" should {
    "more than 5 days before travel" in {
      val controller = app.injector.instanceOf[PreviousDeclarationDetailsController]
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 2, 1)

      val result = controller.withinTimerange(travelDate, now)
      result mustBe false
    }

    "within 5 days of travel" in {
      val controller = app.injector.instanceOf[PreviousDeclarationDetailsController]
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 2, 9)

      val result = controller.withinTimerange(travelDate, now)
      result mustBe true
    }

    "within 30 days to allow update" in {
      val controller = app.injector.instanceOf[PreviousDeclarationDetailsController]
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 2, 11)

      val result = controller.withinTimerange(travelDate, now)
      result mustBe true
    }

    "greated than 30 days after travel" in {
      val controller = app.injector.instanceOf[PreviousDeclarationDetailsController]
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 3, 15)

      val result = controller.withinTimerange(travelDate, now)
      result mustBe false
    }
  }

  "list of goods" should {
    "only contain goods when no amendments" in {
      val controller = app.injector.instanceOf[PreviousDeclarationDetailsController]
      val goods = List.fill(10)(createImport("wine", "99.99"))
      val result = controller.listGoods(goods, List.empty[Amendment])

      result.length mustBe 10
    }

    "only contain amendment that have been Paid/NotRequired" in {
      val controller = app.injector.instanceOf[PreviousDeclarationDetailsController]
      val goods = List.tabulate(10)(idx => createImport(s"item-$idx", "99.99"))

      val amendments = List(createAmendment("amend", Some(Paid)))
      val result = controller.listGoods(goods, amendments)

      result.length mustBe 11
    }

    "only contain amendments that have been Paid or NotRequired and not unpaid" in {
      val controller = app.injector.instanceOf[PreviousDeclarationDetailsController]
      val goods = List.tabulate(10)(idx => createImport(s"item-$idx", "99.99"))

      val amendments = List(
        createAmendment("amend", Some(Paid)),
        createAmendment("amend", None),
        createAmendment("amend", Some(NotRequired))
      )
      val result = controller.listGoods(goods, amendments)

      result.length mustBe 12
    }

    "contain all the goods with amendments at the end" in {
      val controller = app.injector.instanceOf[PreviousDeclarationDetailsController]
      val itemCount = 10
      val goods = List.tabulate(itemCount)(idx => createImport(s"item-$idx", "99.99"))
      val amendments = List(createAmendment("amend", Some(Paid)))
      val result = controller.listGoods(goods, amendments)

      result.length mustBe itemCount + 1
      result.last.categoryQuantityOfGoods.category mustBe "amend"
    }
  }

  "creating a page" should {
    "return 200 if declaration exists and resets the journey" in {
      import mibConf._
      val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
      val client = app.injector.instanceOf[HttpClient]
      val connector = new MibConnector(client, s"$protocol://$host:${WireMockSupport.port}")
      val controller =
        new PreviousDeclarationDetailsController(controllerComponents, actionBuilder, connector, view)

      val sessionId = SessionId()
      val id = DeclarationId("456")
      val created: LocalDateTime = LocalDate.now.atStartOfDay

      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = DeclarationType.Export, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersisted(exportJourney)

      givenPersistedDeclarationIsFound(exportJourney.declarationIfRequiredAndComplete.get, id)

      val request = buildGet(routes.PreviousDeclarationDetailsController.onPageLoad().url, id).withSession("declarationId" -> "456")
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe 200

      contentAsString(eventualResult) must include("cheese")

      import exportJourney._
      val resetJourney = DeclarationJourney(sessionId, declarationType)

      declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.sessionId mustBe resetJourney.sessionId
      declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.declarationType mustBe resetJourney.declarationType
    }

    "return 303 if declaration does NOT exist and resets the journey" in {
      import mibConf._
      val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
      val client = app.injector.instanceOf[HttpClient]
      val connector = new MibConnector(client, s"$protocol://$host:${WireMockSupport.port}")
      val controller =
        new PreviousDeclarationDetailsController(controllerComponents, actionBuilder, connector, view)

      val sessionId = SessionId()
      val id = DeclarationId("456")
      val created = LocalDate.now.atStartOfDay

      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = DeclarationType.Export, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersisted(exportJourney)

      givenPersistedDeclarationIsFound(exportJourney.declarationIfRequiredAndComplete.get, id)

      val request =
        buildGet(routes.PreviousDeclarationDetailsController.onPageLoad().url, SessionId()).withSession("declarationId" -> "987")
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe 303

      contentAsString(eventualResult) mustNot include("cheese")

      import exportJourney._
      val resetJourney = DeclarationJourney(sessionId, declarationType)

      declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.sessionId mustBe resetJourney.sessionId
      declarationJourneyRepository.findBySessionId(sessionId).futureValue.get.declarationType mustBe resetJourney.declarationType
    }

  }
}

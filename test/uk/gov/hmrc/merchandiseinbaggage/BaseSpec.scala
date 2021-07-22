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

package uk.gov.hmrc.merchandiseinbaggage

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, DefaultActionBuilder}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DefaultDB
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggage.config.{AppConfig, MongoConfiguration}
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyActionProvider
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BaseSpec extends AnyWordSpec with Matchers

trait BaseSpecWithApplication
    extends BaseSpec with GuiceOneServerPerSuite with MongoConfiguration with ScalaFutures with BeforeAndAfterEach with BeforeAndAfterAll {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  lazy val defaultBuilder = injector.instanceOf[DefaultActionBuilder]
  implicit lazy val appConfig = injector.instanceOf[AppConfig]
  def messagesApi = app.injector.instanceOf[MessagesApi]
  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  implicit val messages: Messages = messagesApi.preferred(fakeRequest)
  implicit val headerCarrier = HeaderCarrier()

  lazy val injector: Injector = app.injector

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(Map(
        "play.http.router"                                   -> "testOnlyDoNotUseInAppConf.Routes",
        "microservice.services.address-lookup-frontend.port" -> WireMockSupport.port,
        "microservice.services.payment.port"                 -> WireMockSupport.port,
        "microservice.services.merchandise-in-baggage.port"  -> WireMockSupport.port,
        "microservice.services.auth.port"                    -> WireMockSupport.port,
        "microservice.services.tps-payments-backend.port"    -> WireMockSupport.port,
        "microservice.services.auditing.port"                -> WireMockSupport.port
      ))
      .build()

  lazy val declarationJourneyRepository: DeclarationJourneyRepository = injector.instanceOf[DeclarationJourneyRepository]

  override def beforeEach(): Unit = declarationJourneyRepository.deleteAll().futureValue

  private lazy val db: () => DefaultDB = app.injector.instanceOf[ReactiveMongoComponent].mongoConnector.db
  private val ttlSeconds = 100

  lazy val stubRepo: DeclarationJourney => DeclarationJourneyRepository = declarationJourney =>
    new DeclarationJourneyRepository(db, ttlSeconds) {
      override def insert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] = Future.successful(declarationJourney)
      override def findBySessionId(sessionId: SessionId): Future[Option[DeclarationJourney]] = Future.successful(Some(declarationJourney))
      override def upsert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] = Future.successful(declarationJourney)
  }

  lazy val stubStride: StrideAuthAction = app.injector.instanceOf[StrideAuthAction]

  lazy val stubProvider: DeclarationJourney => DeclarationJourneyActionProvider = declarationJourney =>
    new DeclarationJourneyActionProvider(defaultBuilder, stubRepo(declarationJourney), stubStride)

  def givenADeclarationJourneyIsPersisted(declarationJourney: DeclarationJourney): DeclarationJourney =
    declarationJourneyRepository.insert(declarationJourney).futureValue
}

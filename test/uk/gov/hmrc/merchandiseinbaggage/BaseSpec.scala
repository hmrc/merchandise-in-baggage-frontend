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

package uk.gov.hmrc.merchandiseinbaggage

import akka.actor.ActorSystem
import org.mongodb.scala.model.Filters
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{ApplicationLifecycle, Injector}
import play.api.mvc.{AnyContentAsEmpty, DefaultActionBuilder}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.{Application, Configuration, inject}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyActionProvider
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.scheduler.SchedulingActor.UpdateDocumentsClass
import uk.gov.hmrc.merchandiseinbaggage.scheduler.{SchedulingActor, UpdateCreatedAtFieldsJob}
import uk.gov.hmrc.merchandiseinbaggage.service.DocumentUpdateService
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.mongo.MongoComponent

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

trait BaseSpec extends AnyWordSpec with Matchers with WireMockSupport

trait BaseSpecWithApplication extends BaseSpec with GuiceOneServerPerSuite with ScalaFutures {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  lazy val defaultBuilder: DefaultActionBuilder             = injector.instanceOf[DefaultActionBuilder]
  implicit lazy val appConfig: AppConfig                    = injector.instanceOf[AppConfig]
  def messagesApi: MessagesApi                              = app.injector.instanceOf[MessagesApi]
  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  implicit val messages: Messages                           = messagesApi.preferred(fakeRequest)
  implicit val headerCarrier: HeaderCarrier                 = HeaderCarrier()

  lazy val injector: Injector = app.injector

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(
        inject.bind[DocumentUpdateService].to[FakeDocumentUpdateService],
        inject.bind[UpdateCreatedAtFieldsJob].to[FakeUpdateCreatedAtFieldsJob]
      )
      .configure(
        Map(
          "play.http.router"                                   -> "testOnlyDoNotUseInAppConf.Routes",
          "microservice.services.address-lookup-frontend.port" -> WireMockSupport.port,
          "microservice.services.payment.port"                 -> WireMockSupport.port,
          "microservice.services.merchandise-in-baggage.port"  -> WireMockSupport.port,
          "microservice.services.auth.port"                    -> WireMockSupport.port,
          "microservice.services.tps-payments-backend.port"    -> WireMockSupport.port,
          "microservice.services.auditing.port"                -> WireMockSupport.port
        )
      )
      .build()

  lazy val declarationJourneyRepository: DeclarationJourneyRepository =
    injector.instanceOf[DeclarationJourneyRepository]

  def deleteAll(): Unit =
    declarationJourneyRepository.collection.deleteMany(Filters.empty()).toFuture().map(_ => ()).futureValue

  override def beforeEach(): Unit = deleteAll()

  private lazy val db = injector.instanceOf[MongoComponent]

  lazy val stubRepo: DeclarationJourney => DeclarationJourneyRepository = declarationJourney =>
    new DeclarationJourneyRepository(db, appConfig) {
      override def findBySessionId(sessionId: SessionId): Future[Option[DeclarationJourney]]  =
        Future.successful(Some(declarationJourney))
      override def upsert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] =
        Future.successful(declarationJourney)
    }

  lazy val stubStride: StrideAuthAction = app.injector.instanceOf[StrideAuthAction]

  lazy val stubProvider: DeclarationJourney => DeclarationJourneyActionProvider = declarationJourney =>
    new DeclarationJourneyActionProvider(defaultBuilder, stubRepo(declarationJourney), stubStride)

  def givenADeclarationJourneyIsPersisted(declarationJourney: DeclarationJourney): DeclarationJourney =
    declarationJourneyRepository.upsert(declarationJourney).futureValue
}

class FakeDocumentUpdateService extends DocumentUpdateService {
  override val jobName: String = "update-created-at-field-job"

  override def invoke(implicit ec: ExecutionContext): Future[Boolean] = Future.successful(true)
}

class FakeUpdateCreatedAtFieldsJob @Inject() (
  val config: Configuration,
  val service: FakeDocumentUpdateService,
  val applicationLifecycle: ApplicationLifecycle
) extends UpdateCreatedAtFieldsJob {

  override def jobName: String                                       = "update-created-at-field-job"
  override val scheduledMessage: SchedulingActor.ScheduledMessage[_] = UpdateDocumentsClass(service)
  override val actorSystem: ActorSystem                              = ActorSystem(jobName)
}

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

import com.github.tomakehurst.wiremock.WireMockServer
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository

trait BaseSpec extends AnyWordSpec with Matchers

trait BaseSpecWithApplication
    extends BaseSpec with GuiceOneServerPerSuite with MongoConfiguration with ScalaFutures with BeforeAndAfterEach {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  def messagesApi = app.injector.instanceOf[MessagesApi]
  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  lazy val injector: Injector = app.injector

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(Map(
        "play.http.router"                                   -> "testOnlyDoNotUseInAppConf.Routes",
        "microservice.services.address-lookup-frontend.port" -> WireMockSupport.port,
        "microservice.services.currency-conversion.port"     -> WireMockSupport.port,
        "microservice.services.payment.port"                 -> WireMockSupport.port,
        "microservice.services.merchandise-in-baggage.port"  -> WireMockSupport.port
      ))
      .build()

  lazy val declarationJourneyRepository: DeclarationJourneyRepository = injector.instanceOf[DeclarationJourneyRepository]

  override def beforeEach(): Unit = declarationJourneyRepository.deleteAll().futureValue
}

trait WireMockSupport extends BeforeAndAfterEach { this: Suite =>
  val wireMockServer = new WireMockServer(WireMockSupport.port)

  override def beforeEach(): Unit = {
    super.beforeEach()
    wireMockServer.start()
  }

  override def afterEach(): Unit =
    wireMockServer.stop()
}

object WireMockSupport {
  val port: Int = 17777
}

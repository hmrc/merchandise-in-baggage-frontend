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

package uk.gov.hmrc.merchandiseinbaggagefrontend

import com.github.tomakehurst.wiremock.WireMockServer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.Injector
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository

trait BaseSpec extends AnyWordSpec with Matchers

trait BaseSpecWithApplication extends BaseSpec
  with GuiceOneServerPerSuite with MongoConfiguration with ScalaFutures with BeforeAndAfterEach {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  lazy val injector: Injector = app.injector

  lazy val declarationJourneyRepository: DeclarationJourneyRepository = app.injector.instanceOf[DeclarationJourneyRepository]
}


trait BaseSpecWithWireMock extends BaseSpecWithApplication {
  val paymentMockServer = new WireMockServer(9057)

  override def beforeEach: Unit = paymentMockServer.start()

  override def afterEach: Unit = paymentMockServer.stop()
}

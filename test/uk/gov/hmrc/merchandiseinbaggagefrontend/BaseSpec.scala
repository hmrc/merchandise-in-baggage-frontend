/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend

import com.github.tomakehurst.wiremock.WireMockServer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.MessagesApi
import play.api.inject.Injector
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.{AppConfig, MongoConfiguration}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.SessionId
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository

trait BaseSpec extends AnyWordSpec with Matchers

trait BaseSpecWithApplication extends BaseSpec
  with GuiceOneServerPerSuite with MongoConfiguration with ScalaFutures with BeforeAndAfterEach {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  lazy val injector: Injector = app.injector

  implicit lazy val appConfig: AppConfig = injector.instanceOf[AppConfig]

  lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  lazy val controllerComponents: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val declarationJourneyRepository: DeclarationJourneyRepository = app.injector.instanceOf[DeclarationJourneyRepository]

  def buildGet(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def buildGet(url: String, sessionId: SessionId): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withSession((SessionKeys.sessionId, sessionId.value)).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def buildPost(url: String): FakeRequest[AnyContentAsEmpty.type] = FakeRequest(POST, url).withCSRFToken
    .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
}


trait BaseSpecWithWireMock extends BaseSpecWithApplication {

  val paymentMockServer = new WireMockServer(9057)

  override def beforeEach: Unit = paymentMockServer.start()

  override def afterEach: Unit = paymentMockServer.stop()
}

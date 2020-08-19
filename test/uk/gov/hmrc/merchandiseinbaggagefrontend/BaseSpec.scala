/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend

import com.github.tomakehurst.wiremock.WireMockServer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ErrorTemplate


trait BaseSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach

trait BaseSpecWithApplication extends BaseSpec with GuiceOneAppPerSuite {
  lazy val injector: Injector = app.injector
  lazy val messagesApi = app.injector.instanceOf[MessagesApi]
  lazy val component = app.injector.instanceOf[MessagesControllerComponents]
  lazy val errorHandlerTemplate = app.injector.instanceOf[ErrorTemplate]
  implicit lazy val appConfig = new AppConfig()
  implicit def messages[A](fakeRequest: FakeRequest[A]): Messages = messagesApi.preferred(fakeRequest)
  implicit val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]

  def buildGet(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
}


trait BaseSpecWithWireMock extends BaseSpecWithApplication {

  val paymentMockServer = new WireMockServer(9057)

  override def beforeEach: Unit = {
    paymentMockServer.start()
  }

  override def afterEach: Unit = {
    paymentMockServer.stop()
  }
}

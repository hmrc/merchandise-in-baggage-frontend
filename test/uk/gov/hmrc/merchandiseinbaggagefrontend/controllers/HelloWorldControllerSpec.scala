/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.HelloWorldPage
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class HelloWorldControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  private val fakeRequest = FakeRequest("GET", "/")
  private val appConfig     = new AppConfig()

  val helloWorldPage: HelloWorldPage = app.injector.instanceOf[HelloWorldPage]

  private val controller = new HelloWorldController(appConfig, stubMessagesControllerComponents(), helloWorldPage)

  "GET /" should {
    "return 200" in {
      val result = controller.helloWorld(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = controller.helloWorld(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }
  }
}

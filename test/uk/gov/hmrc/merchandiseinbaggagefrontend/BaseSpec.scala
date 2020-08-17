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
import play.api.inject.Injector


trait BaseSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach

trait BaseSpecWithApplication extends BaseSpec with GuiceOneAppPerSuite {
  lazy val injector: Injector = app.injector
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

/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import com.github.tomakehurst.wiremock.WireMockServer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec


trait BaseSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach


trait BaseSpecWithWireMock extends BaseSpec {

  val paymentMockServer = new WireMockServer(9662)

  override def beforeEach: Unit = {
    paymentMockServer.start()
  }

  override def afterEach: Unit = {
    paymentMockServer.stop()
  }
}

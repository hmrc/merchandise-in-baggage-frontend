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

package uk.gov.hmrc.merchandiseinbaggage.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.{ConsoleNotifier, Slf4jNotifier}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest.{BeforeAndAfterEach, Suite}

trait WireMockSupport extends BeforeAndAfterEach { this: Suite =>
//  val config = new WireMockConfiguration().notifier(new Slf4jNotifier(true)).port(WireMockSupport.port).notifier(new ConsoleNotifier(true))

//  implicit val wireMockServer = new WireMockServer(config)
  implicit val wireMockServer = new WireMockServer(WireMockSupport.port)

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

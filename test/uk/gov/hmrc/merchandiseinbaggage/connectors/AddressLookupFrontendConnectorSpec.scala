/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import org.scalatest.concurrent.Eventually
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggage.stubs.AddressLookupFrontendStub._

import scala.concurrent.ExecutionContext.Implicits.global

class AddressLookupFrontendConnectorSpec extends BaseSpecWithApplication with Eventually {
  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private lazy val connector             = injector.instanceOf[AddressLookupFrontendConnector]

  "init a journey" in {
    givenInitJourney(wireMockServer)

    val response: String = connector.initJourney(Call("GET", "/address-lookup-return")).futureValue

    response mustBe "/blah"

  }
}

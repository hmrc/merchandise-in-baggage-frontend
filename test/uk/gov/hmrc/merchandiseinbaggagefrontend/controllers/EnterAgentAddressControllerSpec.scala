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

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggagefrontend.connectors.AddressLookupFrontendConnector
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.adresslookup.{Address, Country}
import uk.gov.hmrc.merchandiseinbaggagefrontend.stubs.AddressLookupFrontendStub._

import scala.concurrent.ExecutionContext.Implicits.global

class EnterAgentAddressControllerSpec extends DeclarationJourneyControllerSpec with WireMockSupport {
  private val connector = injector.instanceOf[AddressLookupFrontendConnector]
  private val controller = new EnterAgentAddressController(controllerComponents, actionBuilder, declarationJourneyRepository, connector)

  private val address =
    Address(Seq("address line 1", "address line 2"), Some("AB12 3CD"), Country("GB", Some("United Kingdom")))

  "onPageLoad" must {
    val url = routes.EnterAgentAddressController.onPageLoad().url

    givenInitJourney(wireMockServer)

    "redirect to address-lookup-frontend" when {
      "a declaration journey has been started" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        val request = buildGet(url, sessionId)
        val result = controller.onPageLoad()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/blah")
      }
    }
  }

  "returnFromAddressLookup" must {
    val url = routes.EnterAgentAddressController.returnFromAddressLookup("id").url

    givenConfirmJourney("id", address, wireMockServer)

    "store address and redirect to /enter-eori-number" when {
      "a declaration journey has been started" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)
        val request = buildGet(url, sessionId)
        val result = controller.returnFromAddressLookup("id")(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.EoriNumberController.onPageLoad().url

        startedDeclarationJourney.maybeCustomsAgentAddress mustBe None
        declarationJourneyRepository
          .findBySessionId(sessionId)
          .futureValue
          .get
          .maybeCustomsAgentAddress mustBe Some(address)
      }
    }
  }
}

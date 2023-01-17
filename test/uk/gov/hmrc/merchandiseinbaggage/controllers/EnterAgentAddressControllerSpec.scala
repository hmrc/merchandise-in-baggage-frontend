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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.connectors.AddressLookupFrontendConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.{Address, AddressLookupCountry}
import uk.gov.hmrc.merchandiseinbaggage.stubs.AddressLookupFrontendStub._
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import scala.concurrent.ExecutionContext.Implicits.global

class EnterAgentAddressControllerSpec extends DeclarationJourneyControllerSpec with WireMockSupport {
  "returnFromAddressLookup" must {
    val connector = injector.instanceOf[AddressLookupFrontendConnector]
    val controller = new EnterAgentAddressController(controllerComponents, actionBuilder, declarationJourneyRepository, connector)
    val url = routes.EnterAgentAddressController.returnFromAddressLookup("id").url
    val address =
      Address(Seq("address line 1", "address line 2"), Some("AB12 3CD"), AddressLookupCountry("GB", Some("United Kingdom")))

    givenConfirmJourney("id", address, wireMockServer)

    s"store address and redirect to ${routes.EoriNumberController.onPageLoad}" when {
      "a declaration journey has been started" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney)
        val request = buildGet(url, aSessionId)
        val result = controller.returnFromAddressLookup("id")(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.EoriNumberController.onPageLoad.url

        startedImportJourney.maybeCustomsAgentAddress mustBe None
        declarationJourneyRepository
          .findBySessionId(aSessionId)
          .futureValue
          .get
          .maybeCustomsAgentAddress mustBe Some(address)
      }
    }

    s"store address and redirect to ${routes.CheckYourAnswersController.onPageLoad}" when {
      "a declaration journey is complete" in {
        givenADeclarationJourneyIsPersisted(completedDeclarationJourney)
        val request = buildGet(url, aSessionId)
        val result = controller.returnFromAddressLookup("id")(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.CheckYourAnswersController.onPageLoad.url

        completedDeclarationJourney.maybeCustomsAgentAddress mustNot be(Some(address))
        declarationJourneyRepository
          .findBySessionId(aSessionId)
          .futureValue
          .get
          .maybeCustomsAgentAddress mustBe Some(address)
      }
    }
  }
}

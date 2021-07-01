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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.EssentialAction
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.BaseSpec
import uk.gov.hmrc.merchandiseinbaggage.wiremock.{MockStrideAuth, WireMockSupport}

class DeclarationJourneyActionProviderSpec extends BaseSpec with WireMockSupport {

  "need to be stride authenticated if internal FE flag is set" in new DeclarationJourneyControllerSpec {
    override def fakeApplication(): Application =
      new GuiceApplicationBuilder()
        .configure(
          Map(
            "microservice.services.auth.port" -> WireMockSupport.port,
            "assistedDigital"                 -> true
          ))
        .build()
    val actionProvider = injector.instanceOf[DeclarationJourneyActionProvider]

    MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised()

    val action: EssentialAction = actionProvider.journeyAction { _ =>
      play.api.mvc.Results.Ok("authenticated")
    }

    val result = call(action, buildGet("/", aSessionId))
    status(result) mustBe SEE_OTHER
  }

  "need not to be stride authenticated if internal FE flag is not set" in new DeclarationJourneyControllerSpec {
    val actionProvider = injector.instanceOf[DeclarationJourneyActionProvider]

    val ess: EssentialAction = actionProvider.journeyAction { _ =>
      play.api.mvc.Results.Ok("authenticated")
    }

    val result = call(ess, buildGet("/", aSessionId))
    status(result) mustBe SEE_OTHER
  }
}

/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.auth

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.Results.Ok
import play.api.test.Helpers.*
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithAdminApplication, BaseSpecWithApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StrideAuthActionSpec extends BaseSpecWithApplication {

  val authConnector: AuthConnector      = injector.instanceOf[AuthConnector]
  val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  val action = new StrideAuthAction(authConnector, appConfig, mcc)

  "StrideAuthAction" must {
    "not auth when traffic from public facing domain" in {
      val request = FakeRequest("GET", "/").withHeaders("x-forwarded-host" -> "tax.service.gov.uk")

      action.invokeBlock(
        request,
        { (authRequest: AuthRequest[?]) =>
          authRequest.isAssistedDigital mustBe false
          Future.successful(Ok("200"))
        }
      )
    }

    "auth when traffic from stride domain" in {
      val request = FakeRequest("GET", "/").withHeaders("x-forwarded-host" -> "admin.tax.service.gov.uk")

      val result = action.invokeBlock(
        request,
        { (authRequest: AuthRequest[?]) =>
          authRequest.isAssistedDigital mustBe true
          Future.successful(Ok("200"))
        }
      )

      status(result) mustBe SEE_OTHER
    }
  }
}

class AdminStrideAuthActionSpec extends BaseSpecWithAdminApplication {}

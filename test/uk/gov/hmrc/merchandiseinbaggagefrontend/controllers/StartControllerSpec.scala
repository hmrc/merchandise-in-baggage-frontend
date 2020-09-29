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
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.StartView

import scala.concurrent.ExecutionContext.Implicits.global

class StartControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val view = injector.instanceOf[StartView]
  private lazy val controller = new StartController(controllerComponents, declarationJourneyRepository, view)

  "onPageLoad" must {

    "return OK and correct view for GET" in {
      val getRequest = buildGet(routes.StartController.onPageLoad().url)
      val result = controller.onPageLoad()(getRequest)

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(getRequest, messagesApi.preferred(getRequest), appConfig).toString
    }
  }

  "onSubmit" must {

    "redirect to /select-declaration-type and issue a new session" in {
      val postRequest = buildPost(routes.StartController.onSubmit().url)

      postRequest.session.get(SessionKeys.sessionId).isDefined mustBe false

      val result = controller.onSubmit()(postRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).get mustBe routes.SkeletonJourneyController.selectDeclarationType().url

      session(result).get(SessionKeys.sessionId).isDefined mustBe true
    }
  }

}

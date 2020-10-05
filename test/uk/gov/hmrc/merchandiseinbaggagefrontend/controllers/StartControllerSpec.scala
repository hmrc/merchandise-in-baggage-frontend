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

import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.StartView

import scala.concurrent.ExecutionContext.Implicits.global

class StartControllerSpec extends DeclarationJourneyControllerSpec {
  private lazy val view = injector.instanceOf[StartView]
  private lazy val controller = new StartController(controllerComponents, declarationJourneyRepository, view)

  "onStartExport" must {
    "return OK and correct view for GET" in {
      val getRequest = buildGet(routes.StartController.onStartExport().url)
      val result = controller.onStartExport()(getRequest)
      val content = contentAsString(result)

      status(result) mustEqual OK
      content must include("Declaring goods in your baggage you&#x27;re taking out of the UK to sell")
      content must include("Start now")
    }
  }

  "onStartImport" must {
    "return OK and correct view for GET" in {
      val getRequest = buildGet(routes.StartController.onStartImport().url)
      val result = controller.onStartImport()(getRequest)
      val content = contentAsString(result)

      status(result) mustEqual OK
      content must include("Declaring goods in your baggage you&#x27;re bringing to the UK to sell")
      content must include("Start now")
    }
  }

  "onSubmit" must {
    val url = routes.StartController.onSubmit().url
    val nextUrl = routes.SkeletonJourneyController.selectDeclarationType().url
    val existingSessionKey = "existingSessionKey"
    val existingSessionValue = "existingSessionValue"
    val existingSession = (existingSessionKey, existingSessionValue)
    val requestWithSessionId =
      FakeRequest(POST, url)
        .withSession(existingSession, (SessionKeys.sessionId, sessionId.value))
        .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

    "assign a new session id, persist a declaration journey and redirect to /select-declaration-type" when {
      "a session is not supplied" in {
        val request = FakeRequest(POST, url).withSession(existingSession).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

        declarationJourneyRepository.findAll().futureValue.size mustBe 0

        val result = controller.onSubmit()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustBe nextUrl
        session(result).get(SessionKeys.sessionId).isDefined mustBe true
        session(result).get(existingSessionKey) mustBe Some(existingSessionValue)

        declarationJourneyRepository.findAll().futureValue.size mustBe 1
      }
    }

    "persist a declaration journey and redirect to /select-declaration-type" when {
      "a session is supplied but no declaration journey is associated with it" in {
        declarationJourneyRepository.findBySessionId(sessionId).futureValue.isDefined mustBe false

        val result = controller.onSubmit()(requestWithSessionId)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustBe nextUrl
        session(result).get(SessionKeys.sessionId) mustBe Some(sessionId.value)
        session(result).get(existingSessionKey) mustBe Some(existingSessionValue)

        declarationJourneyRepository.findBySessionId(sessionId).futureValue.isDefined mustBe true
      }
    }

    "redirect to /select-declaration-type" when {
      "a session is supplied and a declaration journey is associated with it" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onSubmit()(requestWithSessionId)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustBe nextUrl
        session(result).get(SessionKeys.sessionId) mustBe Some(sessionId.value)
        session(result).get(existingSessionKey) mustBe Some(existingSessionValue)

        declarationJourneyRepository.findBySessionId(sessionId).futureValue.isDefined mustBe true
      }
    }
  }
}

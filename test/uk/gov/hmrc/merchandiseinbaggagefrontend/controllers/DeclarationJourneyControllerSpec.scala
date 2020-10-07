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

import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.{DeclarationJourney, SessionId}
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpecWithApplication, CoreTestData}

trait DeclarationJourneyControllerSpec extends BaseSpecWithApplication with CoreTestData {
  implicit lazy val appConfig: AppConfig = injector.instanceOf[AppConfig]

  lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  lazy val controllerComponents: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val actionBuilder: DeclarationJourneyActionProvider = injector.instanceOf[DeclarationJourneyActionProvider]

  def buildGet(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def buildGet(url: String, sessionId: SessionId): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withSession((SessionKeys.sessionId, sessionId.value)).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def buildPost(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(POST, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def buildPost(url: String, sessionId: SessionId): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(POST, url).withSession((SessionKeys.sessionId, sessionId.value))
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def givenADeclarationJourneyIsPersisted(declarationJourney: DeclarationJourney): DeclarationJourney =
    declarationJourneyRepository.insert(declarationJourney).futureValue

  def anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToLoad(controller: DeclarationJourneyController, url: String): Unit = {
    "redirect to /invalid-request" when {
      "no session id is set" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onPageLoad()(buildGet(url))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.InvalidRequestController.onPageLoad().url
      }

      "a declaration has not been started" in {
        val result = controller.onPageLoad()(buildGet(url, sessionId))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.InvalidRequestController.onPageLoad().url
      }
    }
  }

  def anEndpointRequiringASessionIdAndLinkedDeclarationJourneyToUpdate(controller: DeclarationJourneyUpdateController, url: String): Unit = {
    "redirect to /invalid-request" when {
      "no session id is set" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        val result = controller.onSubmit()(buildPost(url))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.InvalidRequestController.onPageLoad().url
      }

      "a declaration has not been started" in {
        val result = controller.onSubmit()(buildPost(url, sessionId))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.InvalidRequestController.onPageLoad().url
      }
    }
  }
}
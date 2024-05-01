/*
 * Copyright 2024 HM Revenue & Customs
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

import org.apache.pekko.stream.Materializer
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggage.auth.AuthRequest
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

trait DeclarationJourneyControllerSpec extends BaseSpecWithApplication with CoreTestData {
  lazy val controllerComponents: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val actionBuilder: DeclarationJourneyActionProvider    = injector.instanceOf[DeclarationJourneyActionProvider]
  implicit lazy val materializer: Materializer                = injector.instanceOf[Materializer]
  lazy val messageApi: MessagesApi                            = injector.instanceOf[MessagesApi]
  implicit lazy val lang: Lang                                = Lang("en")

  def buildGet(
    url        : String,
    sessionId  : SessionId,
    journey    : DeclarationJourney    = startedImportJourney,
    sessionData: Seq[(String, String)] = Seq.empty,
    headers    : Seq[(String, String)] = Seq.empty
  ): DeclarationJourneyRequest[AnyContentAsEmpty.type] = {
    val baseRequest = FakeRequest(GET, url)
      .withSession(SessionKeys.sessionId -> sessionId.value)
      .withHeaders(headers: _*)
      .withSession(sessionData: _*)
      .withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

    val request =
      if(journey.isAssistedDigital) {
        baseRequest
          .withHeaders("x-forwarded-host" -> "admin.tax.service.gov.uk")
          .withSession(SessionKeys.authToken -> SessionKeys.authToken)
      } else {
        baseRequest.withHeaders("x-forwarded-host" -> "tax.service.gov.uk")
      }

    new DeclarationJourneyRequest(
      journey,
      AuthRequest(request, None, isAssistedDigital = journey.isAssistedDigital)
    )
  }

  def buildPost(
    url      : String,
    sessionId: SessionId,
    journey  : DeclarationJourney = startedImportJourney,
    headers  : Seq[(String, String)] = Seq.empty,
    formData : Seq[(String, String)] = Seq.empty,
  ): DeclarationJourneyRequest[AnyContentAsEmpty.type] = {
    val baseRequest = FakeRequest(POST, url)
      .withSession(SessionKeys.sessionId -> sessionId.value)
      .withHeaders(headers: _*)
      .withFormUrlEncodedBody(formData: _*)
      .withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

    val request =
      if(journey.isAssistedDigital) {
        baseRequest
          .withHeaders("x-forwarded-host" -> "admin.tax.service.gov.uk")
          .withSession(SessionKeys.authToken -> SessionKeys.authToken)
      } else {
        baseRequest.withHeaders("x-forwarded-host" -> "tax.service.gov.uk")
      }

    new DeclarationJourneyRequest(
      journey,
      AuthRequest(request, None, isAssistedDigital = journey.isAssistedDigital)
    )
  }

  def givenADeclarationJourneyIsPersistedWithStub(declarationJourney: DeclarationJourney): DeclarationJourney =
    stubRepo(declarationJourney).findBySessionId(declarationJourney.sessionId).futureValue.get
}

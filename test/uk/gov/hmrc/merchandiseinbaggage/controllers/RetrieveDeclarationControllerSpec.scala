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

import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.RetrieveDeclarationView

class RetrieveDeclarationControllerSpec extends DeclarationJourneyControllerSpec {

  val view = injector.instanceOf[RetrieveDeclarationView]

  def controller(declarationJourney: DeclarationJourney) =
    new RetrieveDeclarationController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  val journey: DeclarationJourney = DeclarationJourney(aSessionId, Import)

  "onPageLoad" should {
    s"return 200 with expected content" in {

      val request = buildGet(routes.RetrieveDeclarationController.onPageLoad.url, aSessionId)
      val eventualResult = controller(journey).onPageLoad(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messageApi(s"retrieveDeclaration.title"))
      result must include(messageApi(s"retrieveDeclaration.heading"))
      result must include(messageApi(s"retrieveDeclaration.p"))

      result must include(messageApi(s"retrieveDeclaration.mibReference.label"))
      result must include(messageApi(s"retrieveDeclaration.mibReference.hint"))

      result must include(messageApi(s"retrieveDeclaration.eori.label"))
      result must include(messageApi(s"retrieveDeclaration.eori.hint"))
    }
  }

  "onSubmit" should {
    s"redirect to /previous-declaration-details after successful form submit" in {
      val request = buildPost(routes.RetrieveDeclarationController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("mibReference" -> "XAMB0000010000", "eori" -> "GB123456780000")

      val eventualResult = controller(journey).onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.RetrieveDeclarationController.onPageLoad().url)
    }

    "return 400 for invalid MibReference" in {
      val request = buildPost(routes.RetrieveDeclarationController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("mibReference" -> "XAMB00000100", "eori" -> "GB123456780000")
      val eventualResult = controller(journey).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi(s"retrieveDeclaration.mibReference.error.invalid"))
    }

    "return 400 for invalid Eori" in {
      val request = buildPost(routes.RetrieveDeclarationController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("mibReference" -> "XAMB0000010000", "eori" -> "GB1234567")
      val eventualResult = controller(journey).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi(s"retrieveDeclaration.eori.error.invalid"))
    }

    "return 400 for invalid form data" in {
      val request = buildPost(routes.RetrieveDeclarationController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("mibReferzzz" -> "XAMB0000010000", "eorizszsz" -> "GB1234567")
      val eventualResult = controller(journey).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi(s"retrieveDeclaration.mibReference.error.required"))
      result must include(messageApi(s"retrieveDeclaration.eori.error.required"))
    }
  }
}

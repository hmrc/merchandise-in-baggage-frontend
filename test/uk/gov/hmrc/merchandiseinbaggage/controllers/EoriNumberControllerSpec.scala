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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo
import uk.gov.hmrc.merchandiseinbaggage.views.html.EoriNumberView

import scala.concurrent.ExecutionContext.Implicits.global

class EoriNumberControllerSpec extends DeclarationJourneyControllerSpec {
  "onSubmit" must {
    "return BAD_REQUEST and errors" when {
      val controller =
        new EoriNumberController(
          controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[EoriNumberView])

      val url = routes.EoriNumberController.onSubmit().url

      "nothing is entered" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(
          maybeIsACustomsAgent = Some(YesNo.No)
        ))

        val result = controller.onSubmit()(buildPost(url, sessionId))
        val content = contentAsString(result)

        status(result) mustEqual BAD_REQUEST
        content must include("Enter an EORI number")
      }

      "invalid value entered" in {
        givenADeclarationJourneyIsPersisted(startedImportJourney.copy(
          maybeIsACustomsAgent = Some(YesNo.No)
        ))

        val request = buildPost(url, sessionId).withFormUrlEncodedBody(("eori", "invalid"))

        val result = controller.onSubmit()(request)
        val content = contentAsString(result)

        status(result) mustEqual BAD_REQUEST
        content must include("Enter a valid EORI number")
      }
    }
  }
}

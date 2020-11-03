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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.TravellerDetailsForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.TravellerDetailsPage

import scala.concurrent.ExecutionContext.Implicits.global

class TravellerDetailsControllerSpec extends DeclarationJourneyControllerSpec {
  "onSubmit" must {
    "return BAD_REQUEST and errors" when {
      "no entry is made" in {
        val controller =
          new TravellerDetailsController(
            controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[TravellerDetailsPage])

        givenADeclarationJourneyIsPersisted(startedImportJourney)

        form.bindFromRequest()(buildPost(routes.GoodsDestinationController.onSubmit().url, sessionId))

        val result = controller.onSubmit()(buildPost(routes.GoodsDestinationController.onSubmit().url, sessionId))
        val content = contentAsString(result)

        status(result) mustEqual BAD_REQUEST

        content must include("Enter the name of the person carrying the goods")
        content must include("First name")
        content must include("Last name")
        content must include("Continue")
        content must include("There is a problem")
        content must include("This field is required")
      }
    }
  }
}

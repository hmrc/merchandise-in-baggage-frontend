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
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.CannotUseServiceView

class CannotUseServiceControllerSpec extends DeclarationJourneyControllerSpec {
  "onPageLoad" must {
    "return OK and render the view" in {
      val view = injector.instanceOf[CannotUseServiceView]
      val controller = new CannotUseServiceController(controllerComponents, view)
      val request = buildGet(routes.CannotUseServiceController.onPageLoad().url)
      val result = controller.onPageLoad()(request)

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(request, messagesApi.preferred(request), appConfig).toString
    }
  }
}

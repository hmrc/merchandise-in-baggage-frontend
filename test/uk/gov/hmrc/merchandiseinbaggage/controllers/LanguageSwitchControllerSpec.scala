/*
 * Copyright 2023 HM Revenue & Customs
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

import org.scalatest.OptionValues
import play.api.i18n.MessagesApi
import play.api.test.Helpers._

class LanguageSwitchControllerSpec extends DeclarationJourneyControllerSpec with OptionValues {

  val controller = new LanguageSwitchController(appConfig, injector.instanceOf[MessagesApi], controllerComponents)

  "switchToLanguage" should {
    "should switch to English" in {

      val request        = buildGet(routes.LanguageSwitchController.switchToLanguage("en").url, aSessionId)
      val eventualResult = controller.switchToLanguage("en")(request)

      status(eventualResult) mustBe 303
      cookies(eventualResult).get("PLAY_LANG").value.value mustEqual "en"
    }

    "should switch to Welsh" in {

      val request        = buildGet(routes.LanguageSwitchController.switchToLanguage("cy").url, aSessionId)
      val eventualResult = controller.switchToLanguage("cy")(request)

      status(eventualResult) mustBe 303
      cookies(eventualResult).get("PLAY_LANG").value.value mustEqual "cy"
    }
  }
}

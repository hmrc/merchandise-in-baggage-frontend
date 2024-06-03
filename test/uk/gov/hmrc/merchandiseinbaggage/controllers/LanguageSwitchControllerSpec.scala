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

import org.scalatest.OptionValues
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.merchandiseinbaggage.auth.StrideAuthAction

import scala.concurrent.ExecutionContext.Implicits.global

class LanguageSwitchControllerSpec extends DeclarationJourneyControllerSpec with OptionValues {

  private val authConnector                      = injector.instanceOf[AuthConnector]
  private val mcc: MessagesControllerComponents  = injector.instanceOf[MessagesControllerComponents]
  private val strideAuthAction: StrideAuthAction = new StrideAuthAction(authConnector, appConfig, mcc)

  private val controller: LanguageSwitchController =
    new LanguageSwitchController(controllerComponents, strideAuthAction)

  "LanguageSwitchController" when {
    "switchToLanguage" should {
      "switch to English" in {

        val request        = buildGet(routes.LanguageSwitchController.switchToLanguage("en").url, aSessionId)
        val eventualResult = controller.switchToLanguage("en")(request)

        status(eventualResult) mustBe SEE_OTHER
        cookies(eventualResult).get("PLAY_LANG").value.value mustEqual "en"
      }

      "switch to Welsh" in {

        val request        = buildGet(routes.LanguageSwitchController.switchToLanguage("cy").url, aSessionId)
        val eventualResult = controller.switchToLanguage("cy")(request)

        status(eventualResult) mustBe SEE_OTHER
        cookies(eventualResult).get("PLAY_LANG").value.value mustEqual "cy"
      }
    }
  }
}

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

package uk.gov.hmrc.merchandiseinbaggage.config

import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggage.model.api.tpspayments.TpsNavigation

class AppConfigSpec extends BaseSpecWithApplication with Matchers {

  "AppConfig" should {
    "return the correct values" in {
      appConfig.languageTranslationEnabled mustBe true

      appConfig.tpsNavigation mustBe TpsNavigation(
        back = "http://localhost:8281/declare-commercial-goods/check-your-answers",
        reset = "http://localhost:8281/declare-commercial-goods/import-export-choice",
        finish = "http://localhost:8281/declare-commercial-goods/declaration-confirmation"
      )

      appConfig.strideRoles mustBe Seq("tps_payment_taker_call_handler", "digital_mib_call_handler")

      appConfig.mongoTTL mustBe 3600

      appConfig.mibCheckEoriUrl mustBe "/declare-commercial-goods/validate/eori/"

      appConfig.isAssistedDigital mustBe false

      appConfig.languageTranslationEnabled mustBe true
    }

    "throw an exception when the config value is not found" in {
      val conf: AppConfig = app.injector.instanceOf[AppConfig]
      val exception = intercept[RuntimeException] {
        conf.config.get[String]("non-existent-key")
      }

      exception.getMessage must include("No configuration setting found for key 'non-existent-key'")
    }
  }

}

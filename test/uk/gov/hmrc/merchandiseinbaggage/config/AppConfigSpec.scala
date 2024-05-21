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

import play.api.{Application, inject}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, FakeUpdateCreatedAtFieldsJob}
import uk.gov.hmrc.merchandiseinbaggage.model.api.tpspayments.TpsNavigation
import uk.gov.hmrc.merchandiseinbaggage.scheduler.UpdateCreatedAtFieldsJob

class AppConfigSpec extends BaseSpecWithApplication {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(
      inject.bind[UpdateCreatedAtFieldsJob].to[FakeUpdateCreatedAtFieldsJob]
    )
    .build()

  "AppConfig" should {

    "return the correct values" in {

      appConfig.strideRoles mustBe Seq("tps_payment_taker_call_handler", "digital_mib_call_handler")
      appConfig.timeout mustBe 900
      appConfig.countdown mustBe 120

      appConfig.paymentsReturnUrl mustBe "http://localhost:8281/declare-commercial-goods/declaration-confirmation"
      appConfig.paymentsBackUrl mustBe "http://localhost:8281/declare-commercial-goods/check-your-answers"

      appConfig.tpsNavigation mustBe TpsNavigation(
        back = "http://localhost:8281/declare-commercial-goods/check-your-answers",
        reset = "http://localhost:8281/declare-commercial-goods/import-export-choice",
        finish = "http://localhost:8281/declare-commercial-goods/declaration-confirmation"
      )

      appConfig.mongoTTL mustBe 3600

      appConfig.feedbackUrl mustBe "http://localhost:9514/feedback/mib"

      appConfig.languageTranslationEnabled mustBe true

      appConfig.mibDeclarationsUrl mustBe "/declare-commercial-goods/declarations"
      appConfig.mibCalculationsUrl mustBe "/declare-commercial-goods/calculations"
      appConfig.mibAmendsPlusExistingCalculationsUrl mustBe "/declare-commercial-goods/amend-calculations"
      appConfig.mibCheckEoriUrl mustBe "/declare-commercial-goods/validate/eori/"

      appConfig.paymentUrl mustBe "http://localhost:9057"
      appConfig.tpsPaymentsBackendUrl mustBe "http://localhost:9125"
      appConfig.merchandiseInBaggageUrl mustBe "http://localhost:8280"
      appConfig.addressLookupFrontendUrl mustBe "http://localhost:9028"
      appConfig.addressLookupCallbackUrl mustBe "http://localhost:8281"
    }

    "throw an exception when the config value is not found" in {
      val exception = intercept[RuntimeException] {
        appConfig.config.get[String]("non-existent-key")
      }

      exception.getMessage must include("No configuration setting found for key 'non-existent-key'")
    }
  }

}

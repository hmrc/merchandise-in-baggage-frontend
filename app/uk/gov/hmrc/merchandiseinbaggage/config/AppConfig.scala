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

import com.google.inject.Inject
import com.typesafe.config.ConfigFactory
import play.api.{Configuration, Environment}
import uk.gov.hmrc.merchandiseinbaggage.model.api.tpspayments.TpsNavigation
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Singleton

@Singleton
class AppConfig @Inject() (val config: Configuration, val env: Environment, servicesConfig: ServicesConfig)()
    extends IsAssistedDigitalConfiguration {

  private val serviceIdentifier = "mib"

  lazy val strideRoles: Seq[String] = config.get[Seq[String]]("stride.roles")
  lazy val timeout: Int             = config.get[Int]("timeout.timeout")
  lazy val countdown: Int           = config.get[Int]("timeout.countdown")

  lazy val paymentsReturnUrl: String = config.get[String]("payments.returnUrl")
  lazy val paymentsBackUrl: String   = config.get[String]("payments.backUrl")

  lazy val tpsNavigation: TpsNavigation = TpsNavigation(
    back = config.get[String]("tps-navigation.back"),
    reset = config.get[String]("tps-navigation.reset"),
    finish = config.get[String]("tps-navigation.finish")
  )

  lazy val mongoTTL: Int = config.get[Int]("mongodb.timeToLiveInSeconds")

  val feedbackUrl: String = {
    val url = config.get[String]("microservice.services.feedback-frontend.url")
    s"$url/$serviceIdentifier"
  }

  lazy val languageTranslationEnabled: Boolean = config.get[Boolean]("features.welsh-translation")

  private lazy val mibBaseUrl: String                   = "/declare-commercial-goods"
  lazy val mibDeclarationsUrl: String                   = s"$mibBaseUrl/declarations"
  lazy val mibCalculationsUrl: String                   = s"$mibBaseUrl/calculations"
  lazy val mibAmendsPlusExistingCalculationsUrl: String = s"$mibBaseUrl/amend-calculations"
  lazy val mibCheckEoriUrl: String                      = s"$mibBaseUrl/validate/eori/"

  lazy val paymentUrl: String               = servicesConfig.baseUrl("payment")
  lazy val tpsPaymentsBackendUrl: String    = servicesConfig.baseUrl("tps-payments-backend")
  lazy val merchandiseInBaggageUrl: String  = servicesConfig.baseUrl("merchandise-in-baggage")
  lazy val addressLookupFrontendUrl: String = servicesConfig.baseUrl("address-lookup-frontend")
  lazy val addressLookupCallbackUrl: String =
    config.get[String]("microservice.services.address-lookup-frontend.callback")
}

trait IsAssistedDigitalConfiguration {
  // to avoid re writing the codebase, need to improve in the future to allow injection
  lazy val isAssistedDigital: Boolean = ConfigFactory.load().getBoolean("assistedDigital")
}

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

package uk.gov.hmrc.merchandiseinbaggage.config

import com.google.inject.Inject
import play.api.{Configuration, Environment}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfigSource.configSource
import uk.gov.hmrc.merchandiseinbaggage.model.api.tpspayments.TpsNavigation

import javax.inject.Singleton

@Singleton
class AppConfig @Inject() (val config: Configuration, val env: Environment)()
    extends MibConfiguration
    with IsAssistedDigitalConfiguration {

  private val serviceIdentifier = "mib"

  private val contactHost: String = configSource("contact-frontend.host").loadOrThrow[String]
  val contactUrl                  = s"$contactHost/contact/contact-hmrc-unauthenticated?service=$serviceIdentifier"

  lazy val strideRoles: Seq[String] = config.get[Seq[String]]("stride.roles")
  lazy val timeout: Int             = configSource("timeout.timeout").loadOrThrow[Int]
  lazy val countdown: Int           = configSource("timeout.countdown").loadOrThrow[Int]

  lazy val paymentsReturnUrl: String = configSource("payments.returnUrl").loadOrThrow[String]
  lazy val paymentsBackUrl: String   = configSource("payments.backUrl").loadOrThrow[String]

  lazy val tpsNavigation: TpsNavigation = configSource("tps-navigation").loadOrThrow[TpsNavigation]

  lazy val mongoTTL: Int = config.get[Int]("mongodb.timeToLiveInSeconds")

  val feedbackUrl: String = {
    val url = configSource("microservice.services.feedback-frontend.url").loadOrThrow[String]
    s"$url/$serviceIdentifier"
  }

  lazy val languageTranslationEnabled: Boolean = configSource("features.welsh-translation").loadOrThrow[Boolean]

}

object AppConfigSource {
  val configSource: String => ConfigSource = ConfigSource.default.at
}

trait MibConfiguration {
  lazy val mibConf: MIBConf                          = configSource("microservice.services.merchandise-in-baggage").loadOrThrow[MIBConf]
  lazy val baseUrl: String                           = "/declare-commercial-goods"
  lazy val declarationsUrl: String                   = s"$baseUrl/declarations"
  lazy val calculationsUrl: String                   = s"$baseUrl/calculations"
  lazy val amendsPlusExistingCalculationsUrl: String = s"$baseUrl/amend-calculations"
  lazy val checkEoriUrl: String                      = s"$baseUrl/validate/eori/"
  lazy val exchangeRateUrl: String                   = s"$baseUrl/exchange-rate-url"
}

final case class MIBConf(protocol: String, host: String, port: Int)

trait IsAssistedDigitalConfiguration {
  lazy val isAssistedDigital: Boolean = configSource("assistedDigital").loadOrThrow[Boolean]
}

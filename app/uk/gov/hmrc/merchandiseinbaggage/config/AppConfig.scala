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

package uk.gov.hmrc.merchandiseinbaggage.config

import play.api.i18n.Lang
import play.api.mvc.Call

import javax.inject.Singleton
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfigSource.configSource
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes

@Singleton
class AppConfig() extends MongoConfiguration with MibConfiguration {

  val serviceIdentifier = "mib"

  val contactHost = configSource("contact-frontend.host").loadOrThrow[String]

  val betaFeedbackUrl = s"$contactHost/contact/beta-feedback-unauthenticated?service=$serviceIdentifier"
  val contactUrl = s"$contactHost/contact/contact-hmrc-unauthenticated?service=$serviceIdentifier"

  lazy val timeout: Int = configSource("timeout.timeout").loadOrThrow[Int]
  lazy val countdown: Int = configSource("timeout.countdown").loadOrThrow[Int]

  lazy val paymentsReturnUrl: String = configSource("payments.returnUrl").loadOrThrow[String]
  lazy val paymentsBackUrl: String = configSource("payments.backUrl").loadOrThrow[String]

  val feedbackUrl: String = {
    val url = configSource("microservice.services.feedback-frontend.url").loadOrThrow[String]
    s"$url/$serviceIdentifier"
  }

  lazy val languageTranslationEnabled: Boolean = configSource("features.welsh-translation").loadOrThrow[Boolean]

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

}

object AppConfigSource {
  val configSource: String => ConfigSource = ConfigSource.default.at
}

trait MongoConfiguration {
  lazy val mongoConf: MongoConf = configSource("mongodb").loadOrThrow[MongoConf]
}

final case class MongoConf(uri: String, host: String = "localhost", port: Int = 27017, collectionName: String = "declaration")

trait MibConfiguration {
  lazy val mibConf: MIBConf = configSource("microservice.services.merchandise-in-baggage").loadOrThrow[MIBConf]
  lazy val baseUrl: String = "/declare-commercial-goods"
  lazy val declarationsUrl: String = s"$baseUrl/declarations"
  lazy val calculationUrl: String = s"$baseUrl/calculation"
  lazy val calculationsUrl: String = s"$baseUrl/calculations"
  lazy val sendEmailsUrl: String = s"$declarationsUrl/sendEmails"
  lazy val checkEoriUrl: String = s"$baseUrl/validate/eori/"
}

final case class MIBConf(protocol: String, host: String, port: Int)

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

package uk.gov.hmrc.merchandiseinbaggage.config

import javax.inject.Singleton
import pureconfig.ConfigSource
import pureconfig.generic.auto._ // Do not remove this
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfigSource.configSource

@Singleton
class AppConfig() extends MongoConfiguration with MibConfiguration with ContactFrontedConfiguration {

  private val serviceIdentifier = "mib"

  val betaFeedbackUrl = s"$cfUrl/contact/beta-feedback-unauthenticated?service=$serviceIdentifier"
  val contactUrl = s"$cfUrl/contact/contact-hmrc-unauthenticated?service=$serviceIdentifier"

  val feedbackUrl: String = {
    val url = configSource("microservice.services.feedback-frontend.url").loadOrThrow[String]
    s"$url/$serviceIdentifier"
  }
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
  lazy val declarationsUrl: String = "/declare-commercial-goods/declarations"
  lazy val sendEmailsUrl: String = "/declare-commercial-goods/declarations/sendEmails"
}

final case class MIBConf(protocol: String, host: String, port: Int)

final case class ContactFrontedConf(host: String, port: Int, protocol: String)

trait ContactFrontedConfiguration {
  lazy val cfConf: ContactFrontedConf = configSource("microservice.services.contact-frontend").loadOrThrow[ContactFrontedConf]

  val cfUrl  = s"${cfConf.protocol}://${cfConf.host}:${cfConf.port}"
}
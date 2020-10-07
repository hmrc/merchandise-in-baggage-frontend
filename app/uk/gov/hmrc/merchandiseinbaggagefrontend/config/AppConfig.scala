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

package uk.gov.hmrc.merchandiseinbaggagefrontend.config

import javax.inject.Singleton
import pureconfig.ConfigSource
import pureconfig.generic.auto._ // Do not remove this
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfigSource.configSource
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.URL

@Singleton
class AppConfig() extends PaymentServiceConf with MongoConfiguration {
  lazy val footerLinkItems: Seq[String] = configSource("footerLinkItems").loadOrThrow[Seq[String]]

  private val serviceIdentifier = "mib"

  private val contactHost = configSource("contact-frontend.host").loadOrThrow[String]

  val betaFeedbackUrl = s"$contactHost/contact/beta-feedback-unauthenticated?service=$serviceIdentifier"
}

trait CurrencyConversionConf {
  lazy val currencyConversionConf: CurrencyConversionConfiguration = configSource("microservice.services.currency-conversion").loadOrThrow[CurrencyConversionConfiguration]
  import currencyConversionConf._
  lazy val currencyConversionBaseUrl = s"$protocol://$host:$port"
}

case class CurrencyConversionConfiguration(protocol: String, host: String, port: String)

trait PaymentServiceConf {
  lazy val paymentServiceConf: PaymentServiceConfiguration = configSource("microservice.services.payment").loadOrThrow[PaymentServiceConfiguration]
  import paymentServiceConf._
  lazy val paymentBaseUri = s"$protocol://$host:$port/"
}

case class PaymentServiceConfiguration(protocol: String, port: Int, host: String, url: URL)

trait MongoConfiguration {
  lazy val mongoConf: MongoConf = configSource("mongodb").loadOrThrow[MongoConf]
}

final case class MongoConf(uri: String, host: String = "localhost", port: Int = 27017, collectionName: String = "declaration")

object AppConfigSource {
  val configSource: String => ConfigSource = ConfigSource.default.at
}
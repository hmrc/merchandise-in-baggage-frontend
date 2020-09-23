/*
 * Copyright 2020 HM Revenue & Customs
 *
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
}

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
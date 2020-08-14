/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.config

import javax.inject.Singleton
import pureconfig.ConfigSource
import pureconfig.generic.auto._ // Do not remove this

@Singleton
class AppConfig() extends PaymentServiceConf {
  private val configSource: String => ConfigSource = ConfigSource.default.at _

  lazy val footerLinkItems: Seq[String] = configSource("footerLinkItems").loadOrThrow[Seq[String]]
}

trait PaymentServiceConf {
  lazy val paymentServiceConf: PaymentServiceConfiguration = ConfigSource.default.at("payment").loadOrThrow[PaymentServiceConfiguration]
  import paymentServiceConf._
  lazy val paymentBaseUri = s"$protocol://$host:$port"
}

case class PaymentServiceConfiguration(protocol: String, port: Int, host: String)

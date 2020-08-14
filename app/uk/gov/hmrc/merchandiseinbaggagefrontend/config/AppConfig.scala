/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import pureconfig.ConfigSource
import pureconfig.generic.auto._ // Do not remove this

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {
  val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())
}

trait PaymentServiceConf {
  lazy val paymentServiceConf: PaymentServiceConfiguration = ConfigSource.default.at("payment").loadOrThrow[PaymentServiceConfiguration]
  import paymentServiceConf._
  lazy val paymentBaseUri = s"$protocol://$host:$port"
}

case class PaymentServiceConfiguration(protocol: String, port: Int, host: String)

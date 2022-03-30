/*
 * Copyright 2022 HM Revenue & Customs
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

import com.google.inject.AbstractModule
import com.google.inject.name.Names.named
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class MerchandiseInBaggageFrontendConfigModule(unused: Environment, configuration: Configuration) extends AbstractModule {
  val servicesConfig: ServicesConfig = new ServicesConfig(configuration)

  def bindBaseUrl(name: String, service: String): Unit =
    bindConstant().annotatedWith(named(name)).to(servicesConfig.baseUrl(service))

  override def configure(): Unit = {
    bindBaseUrl("paymentBaseUrl", "payment")
    bindBaseUrl("tpsBackendBaseUrl", "tps-payments-backend")
    bindBaseUrl("mibBackendBaseUrl", "merchandise-in-baggage")
    bindBaseUrl("addressLookupFrontendBaseUrl", "address-lookup-frontend")
    bindConstant()
      .annotatedWith(named("addressLookupCallback"))
      .to(servicesConfig.getString("microservice.services.address-lookup-frontend.callback"))
    bindConstant()
      .annotatedWith(named("declarationJourneyTimeToLiveInSeconds"))
      .to(configuration.get[Int]("declarationJourneyTimeToLiveInSeconds"))
  }
}

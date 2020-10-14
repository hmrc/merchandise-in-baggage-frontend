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

package uk.gov.hmrc.merchandiseinbaggagefrontend.connectors

import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.MerchandiseInBaggageConf
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.calculation.{CalculationRequest, CalculationResult}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.AmountInPence

import scala.concurrent.Future

trait MerchandiseInBaggageConnector extends MerchandiseInBaggageConf {
  val httpClient: HttpClient

  def getCalculationResult(request: CalculationRequest): Future[CalculationResult] = {
    //TODO actually call backend when it's done, will just println for now
    println(s"Returning dummy CalculationResult for request: $request")
    Future.successful(CalculationResult(AmountInPence(10000), AmountInPence(330), AmountInPence(65)))
  }

}

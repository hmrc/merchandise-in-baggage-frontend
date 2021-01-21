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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResult

case class PaymentCalculation(goods: Goods, calculationResult: CalculationResult)

object PaymentCalculation {
  implicit val format: OFormat[PaymentCalculation] = Json.format[PaymentCalculation]
}

case class PaymentCalculations(paymentCalculations: Seq[PaymentCalculation])

object PaymentCalculations {
  implicit val format: OFormat[PaymentCalculations] = Json.format[PaymentCalculations]
}

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

import java.text.NumberFormat.getCurrencyInstance
import java.util.Locale.UK
import play.api.libs.functional.syntax._

import play.api.libs.json.Format

case class AmountInPence(value: Long) {
  val inPounds: BigDecimal = (BigDecimal(value) / 100).setScale(2)
  val formattedInPounds: String = getCurrencyInstance(UK).format(inPounds)
  val formattedInPoundsUI: String = formattedInPounds.split("\\.00")(0) //TODO not sure if necessary different formats
  lazy val isPaymentRequired: Boolean = inPounds.compareTo(BigDecimal(0.0)) > 0
}

object AmountInPence {
  implicit val format: Format[AmountInPence] = implicitly[Format[Long]].inmap(AmountInPence(_), _.value)

  def fromBigDecimal(in: BigDecimal): AmountInPence = AmountInPence((in * 100).toLong)
}

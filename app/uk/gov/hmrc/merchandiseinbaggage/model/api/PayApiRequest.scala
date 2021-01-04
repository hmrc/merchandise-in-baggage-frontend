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

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.core.AmountInPence
import uk.gov.hmrc.merchandiseinbaggage.utils.ValueClassFormat

case class MibReference(value: String)

object MibReference {
  implicit val format: Format[MibReference] = ValueClassFormat.format(value => MibReference.apply(value))(_.value)
}

case class PayApiRequest(
  mibReference: MibReference,
  amountInPence: AmountInPence,
  vatAmountInPence: AmountInPence,
  dutyAmountInPence: AmountInPence,
  returnUrl: String,
  backUrl: String)

object PayApiRequest {
  implicit val format: Format[PayApiRequest] = Json.format[PayApiRequest]
}

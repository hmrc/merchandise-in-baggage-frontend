/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.api

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggagefrontend.utils.ValueClassFormat


case class MibReference(value: String)
object MibReference {
  implicit val format: Format[MibReference] = ValueClassFormat.format(value => MibReference.apply(value))(_.value)
}

case class AmountInPence(value: Long)
object AmountInPence {
  implicit val format: Format[AmountInPence] = ValueClassFormat.formatDouble(value => AmountInPence.apply(value))(_.value)
}

case class TraderDetails(value: String)
object TraderDetails {
  implicit val format: Format[TraderDetails] = ValueClassFormat.format(value => TraderDetails.apply(value))(_.value)
}

case class MerchandiseDetails(value: String)
object MerchandiseDetails {
  implicit val format: Format[MerchandiseDetails] = ValueClassFormat.format(value => MerchandiseDetails.apply(value))(_.value)
}

case class PaymentRequest(mibReference: MibReference, amountInPence: AmountInPence, vatAmountInPence: AmountInPence,
                          dutyAmountInPence: AmountInPence, traderDetails: TraderDetails, merchandiseDetails: MerchandiseDetails)
object PaymentRequest {
  implicit val format: Format[PaymentRequest] = Json.format[PaymentRequest]
}

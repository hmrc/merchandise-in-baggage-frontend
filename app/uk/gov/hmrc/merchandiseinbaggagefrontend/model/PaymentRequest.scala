/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.model

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggagefrontend.utils.ValueClassFormat


case class ChargeReference(value: String)
object ChargeReference {
  implicit val format: Format[ChargeReference] = ValueClassFormat.format(value => ChargeReference.apply(value))(_.value)
}

case class TaxToPayInPence(value: Int)
object TaxToPayInPence {
  implicit val format: Format[TaxToPayInPence] = ValueClassFormat.format(value => TaxToPayInPence.apply(value.toInt))(_.value.toString)
}

case class TraderDetails(value: String)
object TraderDetails {
  implicit val format: Format[TraderDetails] = ValueClassFormat.format(value => TraderDetails.apply(value))(_.value)
}

case class MerchandiseDetails(value: String)
object MerchandiseDetails {
  implicit val format: Format[MerchandiseDetails] = ValueClassFormat.format(value => MerchandiseDetails.apply(value))(_.value)
}

case class PaymentRequest(chargeReference: ChargeReference, taxToPayInPence: TaxToPayInPence, traderDetails: TraderDetails, merchandiseDetails: MerchandiseDetails)
object PaymentRequest {
  implicit val format: Format[PaymentRequest] = Json.format[PaymentRequest]
}

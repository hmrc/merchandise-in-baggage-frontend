package uk.gov.hmrc.merchandiseinbaggagefrontend.model.calculation

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsVatRate

case class CalculationRequest(amount: BigDecimal, currencyCode: String, vatRate: GoodsVatRate)

object CalculationRequest {
  implicit val format: OFormat[CalculationRequest] = Json.format[CalculationRequest]
}

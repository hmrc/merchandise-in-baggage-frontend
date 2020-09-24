/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.api

import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpec

class PayApitRequestSpec extends BaseSpec {

  "Serialise/Deserialise from/to json to PaymentRequest" in {
    val paymentRequest = PayApitRequest(
      MibReference("MIBI1234567890"),
      AmountInPence(1),
      AmountInPence(2),
      AmountInPence(3),
      TraderDetails("Trader Inc, 239 Old Street, Berlin, Germany, EC1V 9EY"),
      MerchandiseDetails("Parts and technical crew for the forest moon")
    )

    val actual = Json.toJson(paymentRequest).toString

    Json.toJson(paymentRequest) mustBe Json.parse(actual)
  }
}

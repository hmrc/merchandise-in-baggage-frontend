/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.model

import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpec

class PaymentRequestTest extends BaseSpec {

  "Serialise/Deserialise from/to json to PaymentRequest" in {
    val paymentRequest = PaymentRequest(
      ChargeReference("MIBI1234567890"),
      TaxToPayInPence(200099),
      TraderDetails("Trader Inc, 239 Old Street, Berlin, Germany, EC1V 9EY"),
      MerchandiseDetails("Parts and technical crew for the forest moon")
    )

    val actual = Json.toJson(paymentRequest).toString

    Json.toJson(paymentRequest) mustBe Json.parse(actual)
  }

}

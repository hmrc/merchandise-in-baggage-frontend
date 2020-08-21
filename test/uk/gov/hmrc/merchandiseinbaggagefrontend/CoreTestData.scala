/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.{AmountInPence, MerchandiseDetails, MibReference, PayApitRequest, TraderDetails}

trait CoreTestData {

  val payApiRequest: PayApitRequest = PayApitRequest(
    MibReference("MIBI1234567890"),
    AmountInPence(1),
    AmountInPence(2),
    AmountInPence(3),
    TraderDetails("Trader Inc, 239 Old Street, Berlin, Germany, EC1V 9EY"),
    MerchandiseDetails("Parts and technical crew for the forest moon")
  )
}

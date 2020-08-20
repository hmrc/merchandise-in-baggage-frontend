/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.api

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.URL
import uk.gov.hmrc.merchandiseinbaggagefrontend.utils.ValueClassFormat

case class JourneyId(value: String)
object JourneyId {
  implicit val format: Format[JourneyId] = ValueClassFormat.format(value => JourneyId.apply(value))(_.value)
}

case class PayApiResponse(journeyId: JourneyId, nextUrl: URL)

object PayApiResponse {
  implicit val format: Format[PayApiResponse] = Json.format[PayApiResponse]
}



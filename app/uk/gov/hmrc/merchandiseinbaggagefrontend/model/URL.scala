/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.model

import play.api.libs.json.Format
import uk.gov.hmrc.merchandiseinbaggagefrontend.utils.ValueClassFormat

case class URL(value: String) extends AnyVal
object URL {
  implicit val format: Format[URL] = ValueClassFormat.format(value => URL.apply(value))(_.value)
}

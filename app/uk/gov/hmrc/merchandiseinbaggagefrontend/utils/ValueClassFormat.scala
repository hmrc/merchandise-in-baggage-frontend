/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.utils

import play.api.libs.json._

object ValueClassFormat {
  def format[A: Format](fromStringToA: String => A)(fromAToString: A => String) =
    Format[A](
      Reads[A] {
        case JsString(str) => JsSuccess(fromStringToA(str))
        case unknown       => JsError(s"JsString value expected, got: $unknown")
      },
      Writes[A](a => JsString(fromAToString(a)))
    )

  def formatDouble[A: Format](fromNumberToA: Long => A)(fromAToLong: A => Long) =
    Format[A](
      Reads[A] {
        case JsNumber(n) => JsSuccess(fromNumberToA(n.toLong))
        case unknown     => JsError(s"JsString value expected, got: $unknown")
      },
      Writes[A](a => JsNumber(fromAToLong(a)))
    )
}

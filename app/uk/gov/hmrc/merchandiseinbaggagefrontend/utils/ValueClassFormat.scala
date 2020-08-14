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
}

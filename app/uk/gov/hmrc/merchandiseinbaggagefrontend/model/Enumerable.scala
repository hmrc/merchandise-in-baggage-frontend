/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.model

import play.api.libs.json._

trait Enumerable[A] {

  def withName(str: String): Option[A]
}

object Enumerable {

  def apply[A](entries: (String, A)*): Enumerable[A] =
    new Enumerable[A] {
      override def withName(str: String): Option[A] =
        entries.toMap.get(str)
    }

  trait Implicits {

    implicit def reads[A](implicit ev: Enumerable[A]): Reads[A] =
      Reads {
        case JsString(str) =>
          ev.withName(str)
            .map { s =>
              JsSuccess(s)
            }
            .getOrElse(JsError("error.invalid"))
        case _ =>
          JsError("error.invalid")
      }

    implicit def writes[A: Enumerable]: Writes[A] =
      Writes(value => JsString(value.toString))
  }
}

class WithName(string: String) {
  override val toString: String = string
}

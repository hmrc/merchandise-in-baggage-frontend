/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import org.scalatest.{Assertion, OptionValues}
import play.api.data.{Form, FormError}
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpec

trait FormSpec extends BaseSpec with OptionValues {

  def checkForError(form: Form[_], data: Map[String, String], expectedErrors: Seq[FormError]): Assertion =
    form
      .bind(data)
      .fold(
        formWithErrors => {
          for (error <- expectedErrors)
            formWithErrors.errors must contain(FormError(error.key, error.message, error.args))
          formWithErrors.errors.size mustBe expectedErrors.size
        },
        _ => {
          fail("Expected a validation error when binding the form, but it was bound successfully.")
        }
      )

  def error(key: String, value: String, args: Any*) = Seq(FormError(key, value, args))

  lazy val emptyForm: Map[String, String] = Map[String, String]()

}

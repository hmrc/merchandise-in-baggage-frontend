/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms.mappings

import play.api.data.FieldMapping
import play.api.data.Forms.of
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.Enumerable

trait Mappings extends Formatters {

  protected def boolean(requiredKey: String = "error.required",
                        invalidKey: String = "error.boolean"): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey))

  protected def enumerable[A](requiredKey: String = "error.required", invalidKey: String = "error.invalid")(
    implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey))
}

/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.behaviours.BooleanFieldBehaviours

class ExciseAndRestrictedGoodsFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "exciseAndRestrictedGoods.error.required"
  val invalidKey = "error.boolean"

  val form = new ExciseAndRestrictedGoodsFormProvider()()

  ".value" must {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.behaviours.OptionFieldBehaviours
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestination

class GoodsDestinationFormProviderSpec extends OptionFieldBehaviours {

  val form = new GoodsDestinationFormProvider()()

  ".value" must {

    val fieldName = "value"
    val requiredKey = "goodsDestination.error.required"

    behave like optionsField[GoodsDestination](
      form,
      fieldName,
      validValues = GoodsDestination.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}

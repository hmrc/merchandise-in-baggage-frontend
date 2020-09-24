/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import javax.inject.Inject
import play.api.data.Form
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.mappings.Mappings

class ExciseAndRestrictedGoodsFormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("exciseAndRestrictedGoods.error.required")
    )

}

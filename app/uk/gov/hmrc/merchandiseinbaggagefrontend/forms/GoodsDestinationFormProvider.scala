/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import javax.inject.Inject
import play.api.data.Form
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.mappings.Mappings
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestination

class GoodsDestinationFormProvider @Inject() extends Mappings {

  def apply(): Form[GoodsDestination] =
    Form(
      "value" -> enumerable[GoodsDestination]("goodsDestination.error.required")
    )

}

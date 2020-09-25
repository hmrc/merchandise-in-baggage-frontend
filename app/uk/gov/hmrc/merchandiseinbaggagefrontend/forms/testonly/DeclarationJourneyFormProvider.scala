/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms.testonly

import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.mappings.Mappings

class DeclarationJourneyFormProvider extends Mappings {
  def apply(): Form[String] = Form("declarationJourney" -> nonEmptyText)
}

/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import play.api.data.Form
import play.api.data.Forms.{email, mapping}
import play.api.data.validation.Constraints._
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.mappings.Mappings
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.Email

object EnterEmailForm extends Mappings {


  private val emailsMatch: Constraint[Email] = Constraint { value =>
    if(value.email == value.confirmation)
      Valid
    else
      Invalid("enterEmail.error.notMatching")

  }
  val form: Form[Email] = Form(
    mapping(
      "email" -> email,
      "confirmation" -> email
    )(Email.apply)(Email.unapply).verifying(emailsMatch)
  )

}

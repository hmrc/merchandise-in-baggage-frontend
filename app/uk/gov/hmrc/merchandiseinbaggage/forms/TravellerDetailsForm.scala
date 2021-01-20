/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.Mappings
import uk.gov.hmrc.merchandiseinbaggage.model.api.Name

object TravellerDetailsForm extends Mappings {
  val firstName = "firstName"
  val lastName = "lastName"

  private val regex: String = "[A-Za-z '-]*"

  private def isValidName(invalidMessageKey: String): Constraint[String] = Constraint { value: String =>
    if (value.trim matches regex) Valid
    else Invalid(invalidMessageKey)
  }

  val form: Form[Name] = Form(
    mapping(
      firstName ->
        text("travellerDetails.firstName.error.required")
          .verifying(isValidName("travellerDetails.firstName.error.invalid")),
      lastName ->
        text("travellerDetails.lastName.error.required")
          .verifying(isValidName("travellerDetails.lastName.error.invalid")),
    )(Name.apply)(Name.unapply)
  )
}

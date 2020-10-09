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
import play.api.data.Forms.mapping
import play.api.data.Forms._
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.mappings.Mappings
import play.api.data.format.Formats._
import CheckYourAnswersFormProvider._

object CheckYourAnswersFormProvider {
  val taxDue = "taxDue"
}

class CheckYourAnswersFormProvider extends Mappings {

  def apply(): Form[Answers] =
    Form(
      mapping(
        taxDue -> of(doubleFormat)
      )(Answers.apply)(Answers.unapply)
    )

}


case class Answers(taxDue: Double)

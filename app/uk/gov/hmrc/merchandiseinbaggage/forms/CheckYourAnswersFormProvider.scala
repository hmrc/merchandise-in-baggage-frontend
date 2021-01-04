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
import play.api.data.Forms.{mapping, _}
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.Mappings
import uk.gov.hmrc.merchandiseinbaggage.model.core.AmountInPence

object CheckYourAnswersForm extends Mappings {
  val taxDue = "taxDue"

  val form: Form[Answers] =
    Form(
      mapping(
        taxDue -> longNumber
      )(Answers.apply)(answers => Some(answers.taxDue.value))
    )
}

case class Answers(taxDue: AmountInPence)

object Answers {
  def apply(taxDue: Long): Answers = Answers(AmountInPence(taxDue))
}

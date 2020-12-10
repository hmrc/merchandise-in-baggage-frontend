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

package uk.gov.hmrc.merchandiseinbaggage.forms

import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.Mappings
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationType, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo.Yes

object EoriNumberForm extends Mappings {

  private val eoriRegex: String = "^GB[0-9]{12}$"

  private val isValidEori: Constraint[String] = Constraint { value: String =>
    if (value matches (eoriRegex)) Valid
    else Invalid("eoriNumber.error.invalid")
  }

  private def agentOrTrader(customsAgent: YesNo): String =
    if(customsAgent == Yes) "agent" else "trader"

  def form(customsAgent: YesNo, declarationType: DeclarationType): Form[String] = Form(
    "eori" ->
      text(s"eoriNumber.${agentOrTrader(customsAgent)}.$declarationType.error.required")
        .verifying(isValidEori)
  )

}

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

import play.api.data.{Form, FormError}
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.Mappings
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.Yes

object EoriNumberForm extends Mappings {

  private val eoriRegex: String = "^GB[0-9]{12}$"
  val fieldName = "eori"

  private val isValidEori: Constraint[String] = Constraint { value: String =>
    if (value matches eoriRegex) Valid
    else Invalid("eoriNumber.error.invalid")
  }

  private def agentOrTrader(customsAgent: YesNo): String =
    if (customsAgent == Yes) "agent" else "trader"

  def form(customsAgent: YesNo, declarationType: DeclarationType): Form[String] = Form(
    fieldName ->
      eori(s"eoriNumber.${agentOrTrader(customsAgent)}.$declarationType.error.required")
        .verifying(isValidEori)
  )

  def formWithError(customsAgent: YesNo, declarationType: DeclarationType, eori: String): Form[String] =
    form(customsAgent, declarationType)
      .withError(FormError(fieldName, "eoriNumber.error.notFound"))
      .bind(Map(fieldName -> eori))
}

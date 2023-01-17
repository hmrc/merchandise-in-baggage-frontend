/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.Yes

object EoriNumberForm extends Validators {

  val fieldName = "eori"

  private def agentOrTrader(customsAgent: YesNo): String =
    if (customsAgent == Yes) "agent" else "trader"

  def form(customsAgent: YesNo, declarationType: DeclarationType): Form[String] = Form(
    fieldName ->
      eori(s"eoriNumber.${agentOrTrader(customsAgent)}.$declarationType.error.required")
        .verifying(isValidEori())
  )

  def formWithError(customsAgent: YesNo, declarationType: DeclarationType, eori: String): Form[String] =
    form(customsAgent, declarationType)
      .withError(FormError(fieldName, "eoriNumber.error.notFound"))
      .fill(eori)
}

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
import play.api.data.Forms.mapping
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.merchandiseinbaggage.forms.mappings.Mappings
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Eori, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core.RetrieveDeclaration

object RetrieveDeclarationForm extends Mappings {

  protected val referenceFormat = """^X([A-Z])MB(\d{10})$"""
  private val eoriRegex: String = "^GB[0-9]{12}$"

  private val isValidEori: Constraint[String] = Constraint { value: String =>
    if (value matches eoriRegex) Valid
    else Invalid("retrieveDeclaration.eori.error.invalid")
  }

  private def isValidMibRef(value: String) = value.matches(referenceFormat)

  val form: Form[RetrieveDeclaration] = Form(
    mapping(
      "mibReference" ->
        text("retrieveDeclaration.mibReference.error.required")
          .verifying("retrieveDeclaration.mibReference.error.invalid", v => isValidMibRef(v)),
      "eori" ->
        eori("retrieveDeclaration.eori.error.required")
          .verifying(isValidEori),
    )((mibRef, eori) => RetrieveDeclaration(MibReference(mibRef), Eori(eori)))(rd => Some((rd.mibReference.value, rd.eori.value)))
  )
}

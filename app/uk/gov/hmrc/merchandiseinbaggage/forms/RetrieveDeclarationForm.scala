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

import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Eori, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core.RetrieveDeclaration

object RetrieveDeclarationForm extends Validators {

  val form: Form[RetrieveDeclaration] = Form(
    mapping(
      "mibReference" ->
        text("retrieveDeclaration.mibReference.error.required")
          .verifying(isValidMibRef),
      "eori" ->
        eori("retrieveDeclaration.eori.error.required")
          .verifying(isValidEori("retrieveDeclaration.eori.error.invalid"))
    )((mibRef, eori) => RetrieveDeclaration(MibReference(mibRef), Eori(eori)))(rd => Some((rd.mibReference.value, rd.eori.value)))
  )
}

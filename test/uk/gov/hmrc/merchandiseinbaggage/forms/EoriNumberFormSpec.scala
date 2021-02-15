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

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggage.forms.EoriNumberForm._
import uk.gov.hmrc.merchandiseinbaggage.forms.behaviours.FieldBehaviours
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.Yes

class EoriNumberFormSpec extends FieldBehaviours with PropertyBaseTables {

  forAll(declarationTypesTable) { declarationType =>
    forAll(traderYesOrNoAnswer) { (yesOrNo, traderOrAgent) =>
      s".eori for $yesOrNo $traderOrAgent and $declarationType" must {
        behave like mandatoryField(
          form(yesOrNo, declarationType),
          fieldName,
          requiredError = FormError(fieldName, s"eoriNumber.$traderOrAgent.$declarationType.error.required")
        )
      }

      s"handle whitespace and lowercase for $yesOrNo $traderOrAgent and $declarationType" in {
        form(yesOrNo, declarationType)
          .bind(Map(fieldName -> "gb 12 34 67 80 00 00"))
          .errors mustBe Seq.empty
      }
    }
  }

  ".eori" must {
    "return a form with error and the invalid binded value" in {
      val withError = formWithError(Yes, Import, "12 34")

      withError.errors.head mustBe FormError(fieldName, "eoriNumber.error.notFound")
      withError.data mustBe Map(fieldName -> "12 34")
    }
  }
}

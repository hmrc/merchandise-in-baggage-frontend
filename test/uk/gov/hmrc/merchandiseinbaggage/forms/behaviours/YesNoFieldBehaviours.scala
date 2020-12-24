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

package uk.gov.hmrc.merchandiseinbaggage.forms.behaviours

import play.api.data.{Form, FormError}
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo._

trait YesNoFieldBehaviours extends FieldBehaviours {

  def yesNoField(form: Form[_], fieldName: String, invalidError: FormError): Unit = {

    "bind Yes" in {
      val result = form.bind(Map(fieldName -> "Yes"))
      result.value.value mustBe Yes
    }

    "bind No" in {
      val result = form.bind(Map(fieldName -> "No"))
      result.value.value mustBe No
    }

    "not bind nonYesNos" in {

      forAll(nonYesNos -> "nonYesNo") { nonYesNo =>
        val result = form.bind(Map(fieldName -> nonYesNo)).apply(fieldName)
        result.errors mustEqual Seq(invalidError)
      }
    }
  }
}

/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.BaseSpec
import uk.gov.hmrc.merchandiseinbaggage.forms.CheckYourAnswersForm._

class CheckYourAnswersFormSpec extends BaseSpec {
  "bind tax due in pence to the form" in {
    form.bind(Map(taxDue -> "3012")).value mustBe Some(Answers(3012))
  }

  "return error if incorrect" in {
    form.bind(Map[String, String]()).errors mustBe List(FormError(taxDue, List("error.required"), List()))
  }
}

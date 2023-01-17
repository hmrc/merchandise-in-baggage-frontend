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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class AmountInPenceSpec extends BaseSpec with CoreTestData {

  "format correctly" in {
    AmountInPence(0).formattedInPounds mustBe "£0.00"
    AmountInPence(123).formattedInPounds mustBe "£1.23"
    AmountInPence(1230).formattedInPounds mustBe "£12.30"
    AmountInPence(1200).formattedInPounds mustBe "£12"
    AmountInPence(12000).formattedInPounds mustBe "£120"
    AmountInPence(120000).formattedInPounds mustBe "£1,200"
    AmountInPence(1230000).formattedInPounds mustBe "£12,300"
    AmountInPence(123).fromBigDecimal(10.00) mustBe AmountInPence(1000)
  }
}

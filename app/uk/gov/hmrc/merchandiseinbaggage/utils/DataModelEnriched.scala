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

package uk.gov.hmrc.merchandiseinbaggage.utils

import uk.gov.hmrc.merchandiseinbaggage.model.api.PurchaseDetails
import uk.gov.hmrc.merchandiseinbaggage.model.core.PurchaseDetailsInput

object DataModelEnriched {

  implicit class PurchaseDetailsEnriched(details: PurchaseDetails) {
    import details._

    val numericAmount: BigDecimal = BigDecimal(amount)

    def purchaseDetailsInput: PurchaseDetailsInput = PurchaseDetailsInput(amount, currency.code)
  }
}

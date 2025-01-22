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

package uk.gov.hmrc.merchandiseinbaggage.view

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, NotRequired, Paid}
import uk.gov.hmrc.merchandiseinbaggage.views.ViewUtils.proofOfOriginNeeded
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class ViewUtilsSpec extends BaseSpec with CoreTestData {

  "proofOfOriginNeeded" should {
    "true if Declaration > £1000 paid and no Amendments" in {
      val decl = declaration.copy(
        paymentStatus = Some(Paid),
        maybeTotalCalculationResult = Some(calculationResultsOverLimit)
      )

      proofOfOriginNeeded(decl, isAssistedDigital = false) mustBe true
    }

    "false if Declaration < £1000 paid and no Amendments" in {
      val decl = declaration.copy(
        paymentStatus = Some(Paid),
        maybeTotalCalculationResult = Some(calculationResultsUnderLimit)
      )

      proofOfOriginNeeded(decl, isAssistedDigital = false) mustBe false
    }

    "true if Declaration and Amendments > £1000" in {
      val firstAmendmentModified = declarationWithAmendment.amendments.head.copy(
        maybeTotalCalculationResult = Some(calculationResultsOverLimit),
        paymentStatus = Some(Paid)
      )

      val decl = declarationWithAmendment.copy(
        paymentStatus = Some(Paid),
        maybeTotalCalculationResult = Some(calculationResultsUnderLimit),
        amendments = firstAmendmentModified +: declarationWithAmendment.amendments.tail
      )

      proofOfOriginNeeded(decl, isAssistedDigital = false) mustBe true
    }

    "false if Declaration paid and Amendments > £1000 but UNPAID" in {
      val firstAmendmentModified = declarationWithAmendment.amendments.head.copy(
        maybeTotalCalculationResult = Some(calculationResultsUnderLimit),
        paymentStatus = None
      )

      val decl = declarationWithAmendment.copy(
        paymentStatus = Some(Paid),
        maybeTotalCalculationResult = Some(calculationResultsUnderLimit),
        amendments = firstAmendmentModified +: declarationWithAmendment.amendments.tail
      )

      proofOfOriginNeeded(decl, isAssistedDigital = false) mustBe false
    }

    "true if Declaration paid and 3 Amendments last > £1000" in {
      val amendmentsModified: Seq[Amendment] = declarationWith3Amendment.amendments
        .updated(
          0,
          declarationWith3Amendment.amendments.head.copy(
            paymentStatus = None,
            maybeTotalCalculationResult = Some(calculationResultsUnderLimit)
          )
        )
        .updated(
          1,
          declarationWith3Amendment
            .amendments(1)
            .copy(
              paymentStatus = None,
              maybeTotalCalculationResult = Some(calculationResultsUnderLimit)
            )
        )
        .updated(
          2,
          declarationWith3Amendment
            .amendments(2)
            .copy(
              paymentStatus = Some(Paid),
              maybeTotalCalculationResult = Some(calculationResultsOverLimit)
            )
        )

      val decl = declarationWith3Amendment.copy(
        paymentStatus = Some(Paid),
        maybeTotalCalculationResult = Some(calculationResultsUnderLimit),
        amendments = amendmentsModified
      )

      proofOfOriginNeeded(decl, isAssistedDigital = false) mustBe true
    }

    "false if Declaration paid and 3 Amendments last No payment" in {
      val amendmentsModified: Seq[Amendment] = declarationWith3Amendment.amendments
        .updated(
          0,
          declarationWith3Amendment.amendments.head.copy(
            paymentStatus = None,
            maybeTotalCalculationResult = Some(calculationResultsUnderLimit)
          )
        )
        .updated(
          1,
          declarationWith3Amendment
            .amendments(1)
            .copy(
              paymentStatus = Some(Paid),
              maybeTotalCalculationResult = Some(calculationResultsUnderLimit)
            )
        )
        .updated(
          2,
          declarationWith3Amendment
            .amendments(2)
            .copy(
              paymentStatus = Some(NotRequired),
              maybeTotalCalculationResult = Some(calculationResultsUnderLimit)
            )
        )

      val decl = declarationWith3Amendment.copy(
        paymentStatus = Some(Paid),
        maybeTotalCalculationResult = Some(calculationResultsUnderLimit),
        amendments = amendmentsModified
      )

      proofOfOriginNeeded(decl, isAssistedDigital = false) mustBe true
    }

    "false if Declaration is Export" in {
      val decl = declaration.copy(declarationType = Export)

      proofOfOriginNeeded(decl, isAssistedDigital = false) mustBe false
    }
  }
}

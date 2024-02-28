/*
 * Copyright 2024 HM Revenue & Customs
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

import com.softwaremill.quicklens._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.{NotRequired, Paid}
import uk.gov.hmrc.merchandiseinbaggage.views.ViewUtils.proofOfOriginNeeded
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class ViewUtilsSpec extends BaseSpec with CoreTestData {

  "proofOfOriginNeeded" should {
    "true if Declaration > £1000 paid and no Amendments" in {
      val decl = declaration
        .modify(_.paymentStatus)
        .setTo(Some(Paid))
        .modify(_.maybeTotalCalculationResult)
        .setTo(Some(calculationResultsOverLimit))

      proofOfOriginNeeded(decl) mustBe true
    }

    "false if Declaration < £1000 paid and no Amendments" in {
      val decl = declaration
        .modify(_.paymentStatus)
        .setTo(Some(Paid))
        .modify(_.maybeTotalCalculationResult)
        .setTo(Some(calculationResultsUnderLimit))

      proofOfOriginNeeded(decl) mustBe false
    }

    "true if Declaration and Amendments > £1000" in {
      val decl = declarationWithAmendment
        .modify(_.paymentStatus)
        .setTo(Some(Paid))
        .modify(_.maybeTotalCalculationResult)
        .setTo(Some(calculationResultsUnderLimit))
        .modify(_.amendments.at(0).paymentStatus)
        .setTo(Some(Paid))
        .modify(_.amendments.at(0).maybeTotalCalculationResult)
        .setTo(Some(calculationResultsOverLimit))

      proofOfOriginNeeded(decl) mustBe true
    }

    "false if Declaration paid and Amendments > £1000 but UNPAID" in {
      val decl = declarationWithAmendment
        .modify(_.paymentStatus)
        .setTo(Some(Paid))
        .modify(_.maybeTotalCalculationResult)
        .setTo(Some(calculationResultsUnderLimit))
        .modify(_.amendments.at(0).paymentStatus)
        .setTo(None)
        .modify(_.amendments.at(0).maybeTotalCalculationResult)
        .setTo(Some(calculationResultsUnderLimit))

      proofOfOriginNeeded(decl) mustBe false
    }

    "true if Declaration paid and 3 Amendments last > £1000" in {
      val decl = declarationWith3Amendment
        .modify(_.paymentStatus)
        .setTo(Some(Paid))
        .modify(_.maybeTotalCalculationResult)
        .setTo(Some(calculationResultsUnderLimit))
        .modify(_.amendments.at(0).paymentStatus)
        .setTo(None)
        .modify(_.amendments.at(0).maybeTotalCalculationResult)
        .setTo(Some(calculationResultsUnderLimit))
        .modify(_.amendments.at(1).paymentStatus)
        .setTo(None)
        .modify(_.amendments.at(1).maybeTotalCalculationResult)
        .setTo(Some(calculationResultsUnderLimit))
        .modify(_.amendments.at(2).paymentStatus)
        .setTo(Some(Paid))
        .modify(_.amendments.at(2).maybeTotalCalculationResult)
        .setTo(Some(calculationResultsOverLimit))

      proofOfOriginNeeded(decl) mustBe true
    }

    "false if Declaration paid and 3 Amendments last No payment" in {
      val decl = declarationWith3Amendment
        .modify(_.paymentStatus)
        .setTo(Some(Paid))
        .modify(_.maybeTotalCalculationResult)
        .setTo(Some(calculationResultsUnderLimit))
        .modify(_.amendments.at(0).paymentStatus)
        .setTo(None)
        .modify(_.amendments.at(0).maybeTotalCalculationResult)
        .setTo(Some(calculationResultsUnderLimit))
        .modify(_.amendments.at(1).paymentStatus)
        .setTo(Some(Paid))
        .modify(_.amendments.at(1).maybeTotalCalculationResult)
        .setTo(Some(calculationResultsUnderLimit))
        .modify(_.amendments.at(2).paymentStatus)
        .setTo(Some(NotRequired))
        .modify(_.amendments.at(2).maybeTotalCalculationResult)
        .setTo(Some(calculationResultsUnderLimit))

      proofOfOriginNeeded(decl) mustBe true
    }

    "false if Declaration is Export" in {
      val decl = declaration
        .modify(_.declarationType)
        .setTo(Export)

      proofOfOriginNeeded(decl) mustBe false
    }
  }
}

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

package uk.gov.hmrc.merchandiseinbaggage.viewmodels

import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, JourneyOnFoot}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

import java.time.LocalDate

class DeclarationViewSpec extends BaseSpec with CoreTestData {

  "allGoods" should {
    "return all paid goods when there are no amendments" in {
      DeclarationView.allGoods(declaration) mustBe completedDeclarationJourney.goodsEntries.declarationGoodsIfComplete.get.goods
    }

    "return all goods when there are amendments" in {
      val declarationGoods = completedDeclarationJourney.goodsEntries.declarationGoodsIfComplete.get.goods
      val amendmentGoods = declarationWithPaidAmendment.amendments.flatMap(_.goods.goods)
      DeclarationView.allGoods(declarationWithPaidAmendment) mustBe declarationGoods ++ amendmentGoods
    }
  }

  "totalDutyDue" should {
    "return correct value when there are no amendments" in {
      DeclarationView.totalDutyDue(declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult))) mustBe AmountInPence(100)
    }

    "return correct value when there are amendments" in {
      val declarationDuty: Long =
        declarationWithPaidAmendment.maybeTotalCalculationResult.map(_.totalDutyDue).getOrElse(AmountInPence(0)).value
      val amendmentDuty: Long = declarationWithPaidAmendment.amendments.flatMap(_.maybeTotalCalculationResult.map(_.totalDutyDue.value)).sum
      DeclarationView.totalDutyDue(declarationWithPaidAmendment) mustBe AmountInPence(declarationDuty + amendmentDuty)
    }
  }

  "totalVatDue" should {
    "return correct value when there are no amendments" in {
      DeclarationView.totalVatDue(declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult))) mustBe AmountInPence(100)
    }

    "return correct value when there are amendments" in {
      val declarationDuty: Long =
        declarationWithPaidAmendment.maybeTotalCalculationResult.map(_.totalVatDue).getOrElse(AmountInPence(0)).value
      val amendmentDuty: Long = declarationWithPaidAmendment.amendments.flatMap(_.maybeTotalCalculationResult.map(_.totalVatDue.value)).sum
      DeclarationView.totalDutyDue(declarationWithPaidAmendment) mustBe AmountInPence(declarationDuty + amendmentDuty)
    }
  }

  "totalTaxDue" should {
    "return correct value when there are no amendments" in {
      DeclarationView.totalTaxDue(declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult))) mustBe AmountInPence(100)
    }

    "return correct value when there are amendments" in {
      val declarationDuty: Long =
        declarationWithPaidAmendment.maybeTotalCalculationResult.map(_.totalTaxDue).getOrElse(AmountInPence(0)).value
      val amendmentDuty: Long = declarationWithPaidAmendment.amendments.flatMap(_.maybeTotalCalculationResult.map(_.totalTaxDue.value)).sum
      DeclarationView.totalDutyDue(declarationWithPaidAmendment) mustBe AmountInPence(declarationDuty + amendmentDuty)
    }
  }

  "journeyDateWithInAllowedRange" should {
    "more than 5 days before travel" in {
      val travelDate = LocalDate.now().plusDays(10)
      val updatedDeclaration = declaration.copy(journeyDetails = JourneyOnFoot(journeyPort, travelDate))

      val result = DeclarationView.journeyDateWithInAllowedRange(updatedDeclaration)
      result mustBe false
    }

    "within 5 days of travel" in {
      val travelDate = LocalDate.now().plusDays(2)
      val updatedDeclaration = declaration.copy(journeyDetails = JourneyOnFoot(journeyPort, travelDate))

      val result = DeclarationView.journeyDateWithInAllowedRange(updatedDeclaration)
      result mustBe true
    }

    "within 30 days to allow update" in {
      val travelDate = LocalDate.now().minusDays(20)
      val updatedDeclaration = declaration.copy(journeyDetails = JourneyOnFoot(journeyPort, travelDate))

      val result = DeclarationView.journeyDateWithInAllowedRange(updatedDeclaration)
      result mustBe true
    }

    "greater than 30 days after travel" in {
      val travelDate = LocalDate.now().minusDays(32)
      val updatedDeclaration = declaration.copy(journeyDetails = JourneyOnFoot(journeyPort, travelDate))

      val result = DeclarationView.journeyDateWithInAllowedRange(updatedDeclaration)
      result mustBe false
    }
  }
}

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

package uk.gov.hmrc.merchandiseinbaggage.viewmodels

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api._

import java.time.LocalDate

object DeclarationView {

  def allGoods(declaration: Declaration): Seq[Goods] =
    declaration.declarationGoods.goods ++ validAmendments(declaration).flatMap(_.goods.goods)

  def totalDutyDue(declaration: Declaration): AmountInPence = {
    val originalDuty: Long      = declaration.maybeTotalCalculationResult.map(_.totalDutyDue.value).getOrElse(0)
    val allAmendmentsDuty: Long =
      validAmendments(declaration).flatMap(_.maybeTotalCalculationResult).map(_.totalDutyDue.value).sum
    AmountInPence(originalDuty + allAmendmentsDuty)
  }

  def totalVatDue(declaration: Declaration): AmountInPence = {
    val originalVat: Long      = declaration.maybeTotalCalculationResult.map(_.totalVatDue.value).getOrElse(0)
    val allAmendmentsVat: Long =
      validAmendments(declaration).flatMap(_.maybeTotalCalculationResult).map(_.totalVatDue.value).sum
    AmountInPence(originalVat + allAmendmentsVat)
  }

  def totalTaxDue(declaration: Declaration): AmountInPence = {
    val originalTax: Long      = declaration.maybeTotalCalculationResult.map(_.totalTaxDue.value).getOrElse(0)
    val allAmendmentsTax: Long =
      validAmendments(declaration).flatMap(_.maybeTotalCalculationResult).map(_.totalTaxDue.value).sum
    AmountInPence(originalTax + allAmendmentsTax)
  }

  private def validAmendments(declaration: Declaration) =
    declaration.declarationType match {
      case Export => declaration.amendments
      case Import =>
        declaration.amendments.filter(a => a.paymentStatus.contains(Paid) || a.paymentStatus.contains(NotRequired))
    }

  def journeyDateWithInAllowedRange(declaration: Declaration): Boolean = {
    val now          = LocalDate.now()
    val travelDate   = declaration.journeyDetails.dateOfTravel
    val startOfRange = travelDate.minusDays(5)
    val endOfRange   = travelDate.plusDays(30)

    (now.isAfter(startOfRange) || now.isEqual(startOfRange)) &&
    (now.isBefore(endOfRange) || now.isEqual(endOfRange))
  }
}

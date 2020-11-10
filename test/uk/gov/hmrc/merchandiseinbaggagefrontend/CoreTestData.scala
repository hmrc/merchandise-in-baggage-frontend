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

package uk.gov.hmrc.merchandiseinbaggagefrontend

import java.time.LocalDate

import uk.gov.hmrc.merchandiseinbaggagefrontend.controllers.testonly.TestOnlyController
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.calculation.CalculationResult
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsVatRates.Twenty
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.Ports.{Dover, Heathrow}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.No
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.Currency

trait CoreTestData {
  val payApiRequest: PayApiRequest = PayApiRequest(
    MibReference("MIBI1234567890"),
    AmountInPence(1),
    AmountInPence(2),
    AmountInPence(3)
  )

  val sessionId: SessionId = SessionId()

  val startedImportJourney: DeclarationJourney = DeclarationJourney(sessionId, Import)

  val startedExportJourney: DeclarationJourney = DeclarationJourney(sessionId, Export)

  val startedImportToGreatBritainJourney: DeclarationJourney =
    startedImportJourney.copy(maybeGoodsDestination = Some(GreatBritain))

  val startedImportToNorthernIrelandJourney: DeclarationJourney =
    startedImportJourney.copy(maybeGoodsDestination = Some(NorthernIreland))

  val completedGoodsEntry: GoodsEntry = TestOnlyController.completedGoodsEntry

  val completedDeclarationJourney: DeclarationJourney = TestOnlyController.sampleDeclarationJourney(sessionId)

  val declaration: Declaration = completedDeclarationJourney.declarationIfRequiredAndComplete.get

  val incompleteDeclarationJourney: DeclarationJourney = completedDeclarationJourney.copy(maybeJourneyDetailsEntry = None)

  val aCategoryQuantityOfGoods: CategoryQuantityOfGoods = CategoryQuantityOfGoods("test good", "123")

  val startedGoodsEntry: GoodsEntry = GoodsEntry(Some(aCategoryQuantityOfGoods))

  val importJourneyWithStartedGoodsEntry: DeclarationJourney =
    startedImportToGreatBritainJourney.copy(goodsEntries = GoodsEntries(startedGoodsEntry))

  val importJourneyWithOneCompleteAndOneEmptyGoodsEntry: DeclarationJourney =
    startedImportToGreatBritainJourney.copy(goodsEntries = GoodsEntries(Seq(completedGoodsEntry, GoodsEntry.empty)))

  val importJourneyWithOneCompleteAndOneStartedGoodsEntry: DeclarationJourney =
    startedImportToGreatBritainJourney.copy(goodsEntries = GoodsEntries(Seq(completedGoodsEntry, startedGoodsEntry)))

  val importJourneyWithOneCompleteGoodsEntry: DeclarationJourney =
    startedImportToGreatBritainJourney.copy(goodsEntries = GoodsEntries(completedGoodsEntry))

  val importJourneyWithTwoCompleteGoodsEntries: DeclarationJourney =
    startedImportToGreatBritainJourney.copy(goodsEntries = completedDeclarationJourney.goodsEntries)

  val journeyDate: LocalDate = LocalDate.now

  val doverJourneyEntry: JourneyDetailsEntry = JourneyDetailsEntry(Dover, journeyDate)
  val heathrowJourneyEntry: JourneyDetailsEntry = JourneyDetailsEntry(Heathrow, journeyDate)

  val sparseCompleteDeclarationJourney: DeclarationJourney =
    completedDeclarationJourney.copy(
      maybeIsACustomsAgent = Some(No), maybeJourneyDetailsEntry = Some(JourneyDetailsEntry(Heathrow, journeyDate)))

  val aPurchaseDetails: PurchaseDetails = PurchaseDetails("199.99", Currency("Eurozone", "Euro", "EUR"))
  val aGoods: Goods = Goods(aCategoryQuantityOfGoods, Twenty, "EU", aPurchaseDetails, "123")

  val aCalculationResult: CalculationResult = CalculationResult(AmountInPence(10L), AmountInPence(5), AmountInPence(7))
  val aDeclarationGood: DeclarationGoods = DeclarationGoods(aGoods)
  val aPaymentCalculation: PaymentCalculation = PaymentCalculation(aGoods, aCalculationResult)
  val aPaymentCalculations: PaymentCalculations = PaymentCalculations(Seq(aPaymentCalculation))
}

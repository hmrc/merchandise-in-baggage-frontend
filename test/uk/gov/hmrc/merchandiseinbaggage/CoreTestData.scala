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

package uk.gov.hmrc.merchandiseinbaggage

import java.time.LocalDate

import uk.gov.hmrc.merchandiseinbaggage.controllers.testonly.TestOnlyController
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsVatRates.Twenty
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.No
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResult, CalculationResults}
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.{CheckEoriAddress, CheckResponse, CompanyDetails}
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.PayApiRequest
import uk.gov.hmrc.merchandiseinbaggage.model.api.{ConversionRatePeriod, payapi, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, ExportGoodsEntry, GoodsEntries, ImportGoodsEntry}

trait CoreTestData {
  val payApiRequest: PayApiRequest = payapi.PayApiRequest(
    MibReference("MIBI1234567890"),
    AmountInPence(1),
    AmountInPence(2),
    AmountInPence(3),
    "http://localhost:8281/declare-commercial-goods/declaration-confirmation",
    "http://localhost:8281/declare-commercial-goods/check-your-answers"
  )

  val declarationTypes = List(Import, Export)

  val aSessionId: SessionId = SessionId()

  val startedImportJourney: DeclarationJourney = DeclarationJourney(aSessionId, Import)

  val startedExportJourney: DeclarationJourney = DeclarationJourney(aSessionId, Export)

  val startedExportFromGreatBritain: DeclarationJourney =
    startedExportJourney.copy(maybeGoodsDestination = Some(GreatBritain))

  val startedImportToGreatBritainJourney: DeclarationJourney =
    startedImportJourney.copy(maybeGoodsDestination = Some(GreatBritain))

  val startedImportToNorthernIrelandJourney: DeclarationJourney =
    startedImportJourney.copy(maybeGoodsDestination = Some(NorthernIreland))

  val completedImportGoods: ImportGoodsEntry = TestOnlyController.completedGoodsEntry

  val aImportGoods =
    ImportGoods(
      completedImportGoods.maybeCategoryQuantityOfGoods.get,
      completedImportGoods.maybeGoodsVatRate.get,
      completedImportGoods.maybeProducedInEu.get,
      completedImportGoods.maybePurchaseDetails.get
    )

  val overThresholdGoods: GoodsEntries = GoodsEntries(
    completedImportGoods.copy(
      maybePurchaseDetails = Some(PurchaseDetails("1915", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))))))

  val completedDeclarationJourney: DeclarationJourney = TestOnlyController.sampleDeclarationJourney(aSessionId)

  val declaration: Declaration = completedDeclarationJourney.declarationIfRequiredAndComplete.get

  val incompleteDeclarationJourney: DeclarationJourney = completedDeclarationJourney.copy(maybeJourneyDetailsEntry = None)

  val aCategoryQuantityOfGoods: CategoryQuantityOfGoods = CategoryQuantityOfGoods("test good", "123")

  val startedImportGoods: ImportGoodsEntry = ImportGoodsEntry(Some(aCategoryQuantityOfGoods))

  val startedExportGoods: ExportGoodsEntry = ExportGoodsEntry(Some(aCategoryQuantityOfGoods))

  val importJourneyWithStartedGoodsEntry: DeclarationJourney =
    startedImportToGreatBritainJourney.copy(goodsEntries = GoodsEntries(startedImportGoods))

  val exportJourneyWithStartedGoodsEntry: DeclarationJourney =
    startedExportFromGreatBritain.copy(goodsEntries = GoodsEntries(startedExportGoods))

  val importJourneyWithOneCompleteAndOneEmptyGoodsEntry: DeclarationJourney =
    startedImportToGreatBritainJourney.copy(goodsEntries = GoodsEntries(Seq(completedImportGoods, ImportGoodsEntry())))

  val importJourneyWithOneCompleteAndOneStartedGoodsEntry: DeclarationJourney =
    startedImportToGreatBritainJourney.copy(goodsEntries = GoodsEntries(Seq(completedImportGoods, startedImportGoods)))

  val importJourneyWithOneCompleteGoodsEntry: DeclarationJourney =
    startedImportToGreatBritainJourney.copy(goodsEntries = GoodsEntries(completedImportGoods))

  val importJourneyWithTwoCompleteGoodsEntries: DeclarationJourney =
    startedImportToGreatBritainJourney.copy(goodsEntries = completedDeclarationJourney.goodsEntries)

  val previouslyCompleteJourneyWithIncompleteGoodsEntryAdded: DeclarationJourney =
    completedDeclarationJourney.copy(goodsEntries = GoodsEntries(Seq(completedImportGoods, completedImportGoods, startedImportGoods)))

  val importJourneyWithGoodsOverThreshold: DeclarationJourney =
    startedImportToGreatBritainJourney.copy(goodsEntries = overThresholdGoods)

  val completedImportJourneyWithGoodsOverThreshold: DeclarationJourney =
    completedDeclarationJourney.copy(goodsEntries = overThresholdGoods)

  val journeyDate: LocalDate = LocalDate.now

  val doverJourneyEntry: JourneyDetailsEntry = JourneyDetailsEntry("DVR", journeyDate)
  val heathrowJourneyEntry: JourneyDetailsEntry = JourneyDetailsEntry("LHR", journeyDate)

  val sparseCompleteDeclarationJourney: DeclarationJourney =
    completedDeclarationJourney
      .copy(maybeIsACustomsAgent = Some(No), maybeJourneyDetailsEntry = Some(JourneyDetailsEntry("LHR", journeyDate)))

  val aPurchaseDetails: PurchaseDetails =
    PurchaseDetails("199.99", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European")))
  val aGoods: ImportGoods = ImportGoods(aCategoryQuantityOfGoods, Twenty, YesNoDontKnow.Yes, aPurchaseDetails)

  val aConversionRatePeriod: ConversionRatePeriod = ConversionRatePeriod(journeyDate, journeyDate, "EUR", BigDecimal(1.2))
  val aCalculationResult: CalculationResult =
    CalculationResult(aImportGoods, AmountInPence(10L), AmountInPence(5), AmountInPence(7), Some(aConversionRatePeriod))

  val aCalculationResultWithNoTax: CalculationResult =
    CalculationResult(aImportGoods, AmountInPence(100), AmountInPence(0), AmountInPence(0), Some(aConversionRatePeriod))
  val aDeclarationGood: DeclarationGoods = DeclarationGoods(Seq(aGoods))
  val aCalculationResults: CalculationResults = CalculationResults(Seq(aCalculationResult))

  val aCalculationResultsWithNoTax: CalculationResults = CalculationResults(Seq(aCalculationResultWithNoTax))

  val aEoriNumber: String = "GB025115110987654"
  val aCheckEoriAddress: CheckEoriAddress = CheckEoriAddress("999 High Street", "CityName", "SS99 1AA")
  val aCompanyDetails: CompanyDetails = CompanyDetails("Firstname LastName", aCheckEoriAddress)

  val aCheckResponse: CheckResponse = CheckResponse(aEoriNumber, valid = true, Some(aCompanyDetails))

  def aSuccessCheckResponse(eoriNumber: String = aEoriNumber): String =
    s"""{
       |  "eori": "$eoriNumber",
       |  "valid": true,
       |  "companyDetails": {
       |    "traderName": "Firstname LastName",
       |    "address": {
       |      "streetAndNumber": "999 High Street",
       |      "cityName": "CityName",
       |      "postcode": "SS99 1AA"
       |    }
       |  },
       |  "processingDate": "2021-01-27T11:00:22.522Z[Europe/London]"
       |}""".stripMargin

  val completedExportGoods: ExportGoodsEntry = ExportGoodsEntry(
    Some(aCategoryQuantityOfGoods),
    Some(Country("FR", "title.france", "FR", isEu = true, Nil)),
    Some(PurchaseDetails("99.99", Currency("GBP", "title.british_pounds_gbp", Some("GBP"), Nil)))
  )

  def completedGoodsEntries(declarationType: DeclarationType): GoodsEntries =
    declarationType match {
      case Import => GoodsEntries(completedImportGoods)
      case Export => GoodsEntries(completedExportGoods)
    }
}

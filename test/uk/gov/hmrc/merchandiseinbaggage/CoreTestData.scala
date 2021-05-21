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

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import com.softwaremill.quicklens._
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.testonly.TestOnlyController
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsVatRates.Twenty
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.No
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.{Address, AddressLookupCountry}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, CalculationResult, CalculationResults, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.{CheckEoriAddress, CheckResponse, CompanyDetails}
import uk.gov.hmrc.merchandiseinbaggage.model.api.payapi.PayApiRequest
import uk.gov.hmrc.merchandiseinbaggage.model.api.{ConversionRatePeriod, payapi, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, ExportGoodsEntry, GoodsEntries, ImportGoodsEntry, ThresholdAllowance}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.ServiceTimeoutPage.fakeRequest
import uk.gov.hmrc.merchandiseinbaggage.views.html.{DeclarationConfirmationView, Layout}

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

  val journeyTypes = List(New, Amend)

  val aSessionId: SessionId = SessionId()

  val mibReference = MibReference("XAMB0000010000")
  val eori = Eori("GB123456780000")
  val aDeclarationId = DeclarationId(UUID.randomUUID().toString)

  val startedImportJourney: DeclarationJourney = DeclarationJourney(aSessionId, Import)

  val startedExportJourney: DeclarationJourney = DeclarationJourney(aSessionId, Export)

  val startedExportFromGreatBritain: DeclarationJourney =
    startedExportJourney.copy(maybeGoodsDestination = Some(GreatBritain))

  val startedImportToGreatBritainJourney: DeclarationJourney =
    startedImportJourney.copy(maybeGoodsDestination = Some(GreatBritain))

  val startedImportToNorthernIrelandJourney: DeclarationJourney =
    startedImportJourney.copy(maybeGoodsDestination = Some(NorthernIreland))

  val completedImportGoods: ImportGoodsEntry = TestOnlyController.completedGoodsEntry

  val aCategoryOfGoods: String = "test good"

  val completedExportGoods: ExportGoodsEntry = ExportGoodsEntry(
    Some(aCategoryOfGoods),
    Some(Country("FR", "title.france", "FR", isEu = true, Nil)),
    Some(PurchaseDetails("99.99", Currency("GBP", "title.british_pounds_gbp", Some("GBP"), Nil)))
  )

  val aImportGoods =
    ImportGoods(
      completedImportGoods.maybeCategory.get,
      completedImportGoods.maybeGoodsVatRate.get,
      completedImportGoods.maybeProducedInEu.get,
      completedImportGoods.maybePurchaseDetails.get
    )

  val aExportGoods =
    ExportGoods(
      completedExportGoods.maybeCategory.get,
      completedExportGoods.maybeDestination.get,
      completedExportGoods.maybePurchaseDetails.get
    )

  def overThresholdGoods(declarationType: DeclarationType = Import): GoodsEntries =
    declarationType match {
      case Import =>
        GoodsEntries(completedImportGoods.copy(
          maybePurchaseDetails = Some(PurchaseDetails("1915", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))))))

      case Export =>
        GoodsEntries(completedExportGoods.copy(
          maybePurchaseDetails = Some(PurchaseDetails("1915", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))))))
    }

  val completedDeclarationJourney: DeclarationJourney = TestOnlyController.sampleDeclarationJourney(aSessionId)

  val declaration: Declaration = completedDeclarationJourney.declarationIfRequiredAndComplete.get

  val incompleteDeclarationJourney: DeclarationJourney = completedDeclarationJourney.copy(maybeJourneyDetailsEntry = None)

  val startedImportGoods: ImportGoodsEntry = ImportGoodsEntry(Some(aCategoryOfGoods))

  val startedExportGoods: ExportGoodsEntry = ExportGoodsEntry(Some(aCategoryOfGoods))

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
    startedImportToGreatBritainJourney.copy(goodsEntries = overThresholdGoods(Import))

  val completedImportJourneyWithGoodsOverThreshold: DeclarationJourney =
    completedDeclarationJourney.copy(goodsEntries = overThresholdGoods(Import))

  val journeyDate: LocalDate = LocalDate.now

  val doverJourneyEntry: JourneyDetailsEntry = JourneyDetailsEntry("DVR", journeyDate)
  val heathrowJourneyEntry: JourneyDetailsEntry = JourneyDetailsEntry("LHR", journeyDate)
  val anAddress = Address(Seq("1 Agent Drive", "Agent Town"), Some("AG1 5NT"), AddressLookupCountry("GB", Some("United Kingdom")))

  val sparseCompleteDeclarationJourney: DeclarationJourney =
    completedDeclarationJourney
      .copy(maybeIsACustomsAgent = Some(No), maybeJourneyDetailsEntry = Some(JourneyDetailsEntry("LHR", journeyDate)))

  val startedAmendImportJourney: DeclarationJourney =
    DeclarationJourney(aSessionId, Import).copy(journeyType = Amend)

  val startedAmendExportJourney: DeclarationJourney =
    DeclarationJourney(aSessionId, Export).copy(journeyType = Amend)

  val amendImportJourneyWithGoodsEntries: DeclarationJourney =
    startedAmendImportJourney.copy(goodsEntries = GoodsEntries(completedImportGoods))

  val completeAmendExportJourney: DeclarationJourney =
    startedAmendExportJourney.copy(goodsEntries = GoodsEntries(completedExportGoods))

  val aPurchaseDetails: PurchaseDetails =
    PurchaseDetails("199.99", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European")))
  val aGoods: ImportGoods = ImportGoods(aCategoryOfGoods, Twenty, YesNoDontKnow.Yes, aPurchaseDetails)

  val aConversionRatePeriod: ConversionRatePeriod = ConversionRatePeriod(journeyDate, journeyDate, "EUR", BigDecimal(1.2))
  val aCalculationResult: CalculationResult =
    CalculationResult(aImportGoods, AmountInPence(10L), AmountInPence(5), AmountInPence(7), Some(aConversionRatePeriod))

  val aCalculationResultOverThousand: CalculationResult = aCalculationResult
    .modify(_.goods.producedInEu)
    .setTo(YesNoDontKnow.Yes)
    .modify(_.duty.value)
    .setTo(140000)
    .modify(_.vat.value)
    .setTo(140000)
    .modify(_.gbpAmount.value)
    .setTo(140000)

  val aCalculationResultWithNothingToPay: CalculationResult = aCalculationResult
    .modify(_.goods.producedInEu)
    .setTo(YesNoDontKnow.Yes)
    .modify(_.duty.value)
    .setTo(0)
    .modify(_.vat.value)
    .setTo(0)
    .modify(_.gbpAmount.value)
    .setTo(0)

  val aCalculationResultWithNoTax: CalculationResult =
    CalculationResult(aImportGoods, AmountInPence(100), AmountInPence(0), AmountInPence(0), Some(aConversionRatePeriod))
  val aDeclarationGood: DeclarationGoods = DeclarationGoods(Seq(aGoods))
  val aCalculationResults: CalculationResults = CalculationResults(Seq(aCalculationResult))
  val aCalculationResponse: CalculationResponse = CalculationResponse(CalculationResults(Seq(aCalculationResult)), WithinThreshold)
  val aThresholdAllowance = ThresholdAllowance(aDeclarationGood, aCalculationResponse, GreatBritain)

  val aCalculationResultsWithNoTax: CalculationResults = CalculationResults(Seq(aCalculationResultWithNoTax))

  val aEoriNumber: String = "GB025115110987654"
  val aCheckEoriAddress: CheckEoriAddress = CheckEoriAddress("999 High Street", "CityName", "SS99 1AA")
  val aCompanyDetails: CompanyDetails = CompanyDetails("Firstname LastName", aCheckEoriAddress)

  val aCheckResponse: CheckResponse = CheckResponse(aEoriNumber, valid = true, Some(aCompanyDetails))

  val aAmendment = Amendment(
    1,
    LocalDateTime.now,
    DeclarationGoods(aGoods.copy(category = "more cheese") :: Nil),
    Some(TotalCalculationResult(aCalculationResults, AmountInPence(100), AmountInPence(100), AmountInPence(100), AmountInPence(100))),
    None,
    Some("Digital")
  )

  val aAmendmentPaid = aAmendment
    .modify(_.paymentStatus)
    .setTo(Some(Paid))

  val aAmendmentNotRequired = aAmendment
    .modify(_.paymentStatus)
    .setTo(Some(NotRequired))

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

  def completedGoodsEntries(declarationType: DeclarationType): GoodsEntries =
    declarationType match {
      case Import => GoodsEntries(completedImportGoods)
      case Export => GoodsEntries(completedExportGoods)
    }

  def generateDeclarationConfirmationPage(decType: DeclarationType, purchaseAmount: Long, journeyType: JourneyType = New)(
    implicit app: Application,
    message: Messages,
    appConfig: AppConfig): String = {
    val layout = app.injector.instanceOf[Layout]
    val link = app.injector.instanceOf[uk.gov.hmrc.merchandiseinbaggage.views.html.components.link]
    val dummyAmount = AmountInPence(0)

    def totalCalculationResult: TotalCalculationResult =
      TotalCalculationResult(
        CalculationResults(
          Seq(
            CalculationResult(
              ImportGoods(
                "sock",
                GoodsVatRates.Twenty,
                YesNoDontKnow.Yes,
                PurchaseDetails(purchaseAmount.toString, Currency("GBP", "title.british_pounds_gbp", None, List.empty[String]))
              ),
              AmountInPence(purchaseAmount),
              dummyAmount,
              dummyAmount,
              None
            ))
        ),
        AmountInPence(purchaseAmount),
        dummyAmount,
        dummyAmount,
        dummyAmount
      )

    val sessionId = SessionId()
    val id = DeclarationId("456")
    val created = LocalDateTime.now.withSecond(0).withNano(0)

    val journey: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId, declarationType = decType, createdAt = created, declarationId = id)

    val persistedDeclaration = journey.declarationIfRequiredAndComplete.map { declaration =>
      if (decType == DeclarationType.Import) declaration.copy(maybeTotalCalculationResult = Some(totalCalculationResult))
      else declaration
    }

    val declarationConfirmationView = new DeclarationConfirmationView(layout, null, link)
    val result = declarationConfirmationView.apply(persistedDeclaration.get, journeyType)(fakeRequest, message, appConfig)

    result.body
  }

  def completedAmendedJourney(declarationType: DeclarationType): DeclarationJourney = declarationType match {
    case Import => amendImportJourneyWithGoodsEntries
    case Export => completeAmendExportJourney
  }

  def completedAmendment(declarationType: DeclarationType): Amendment = declarationType match {
    case Import => amendImportJourneyWithGoodsEntries.amendmentIfRequiredAndComplete.get
    case Export => completeAmendExportJourney.amendmentIfRequiredAndComplete.get
  }

  val aTotalCalculationResult =
    TotalCalculationResult(aCalculationResults, AmountInPence(100), AmountInPence(100), AmountInPence(100), AmountInPence(100))

  val declarationWithAmendment = declaration.copy(amendments = Seq(completedAmendment(declaration.declarationType)))

  val declarationWith3Amendment = declaration.copy(
    amendments = Seq(
      completedAmendment(declaration.declarationType),
      completedAmendment(declaration.declarationType),
      completedAmendment(declaration.declarationType)))

  val declarationWithPaidAmendment: Declaration = {
    val paidAmendment = completedAmendment(declaration.declarationType)
      .copy(paymentStatus = Some(Paid), maybeTotalCalculationResult = Some(aTotalCalculationResult))
    declaration
      .copy(paymentStatus = Some(Paid), maybeTotalCalculationResult = Some(aTotalCalculationResult), amendments = Seq(paidAmendment))
  }

  val journeyPort = Port("DVR", "title.dover", isGB = true, List("Port of Dover"))

  implicit class JourneyToDeclaration(declarationJourney: DeclarationJourney) {
    import declarationJourney._
    def toDeclaration =
      Declaration(
        declarationId,
        sessionId,
        declarationType,
        maybeGoodsDestination.get,
        goodsEntries.declarationGoodsIfComplete.get,
        maybeNameOfPersonCarryingTheGoods.getOrElse(Name("xx", "yy")),
        maybeEmailAddress,
        maybeCustomsAgent,
        maybeEori.getOrElse(Eori("GB123")),
        JourneyInSmallVehicle(
          journeyPort,
          maybeJourneyDetailsEntry.getOrElse(JourneyDetailsEntry("BH", LocalDate.now)).dateOfTravel,
          maybeRegistrationNumber.getOrElse("Lx123")
        ),
        createdAt,
        MibReference("xx")
      )
  }

  def createTotalCalculationResult(amount: Long): TotalCalculationResult =
    TotalCalculationResult(
      CalculationResults(
        Seq(
          CalculationResult(aImportGoods, AmountInPence(amount), AmountInPence(5), AmountInPence(7), Some(aConversionRatePeriod))
        )),
      AmountInPence(amount),
      AmountInPence(100),
      AmountInPence(100),
      AmountInPence(100)
    )

  val calculationResultsOverLimit = createTotalCalculationResult(110000L) // £1100
  val calculationResultsUnderLimit = createTotalCalculationResult(40000L) // £400

}

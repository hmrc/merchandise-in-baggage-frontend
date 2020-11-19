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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.core

import java.text.NumberFormat.getCurrencyInstance
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.{LocalDate, LocalDateTime}
import java.util.Locale.UK
import java.util.UUID.randomUUID

import enumeratum.EnumEntry
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.Enum
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.adresslookup.Address
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.MibReference
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.calculation.CalculationRequest
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.Currency
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.MibReferenceGenerator

import scala.collection.immutable

case class SessionId(value: String)

object SessionId {
  implicit val format: Format[SessionId] = implicitly[Format[String]].inmap(SessionId(_), _.value)

  def apply(): SessionId = SessionId(randomUUID().toString)
}

case class PurchaseDetails(amount: String, currency: Currency) {
  override val toString = s"$amount, ${currency.displayName}"

  val numericAmount: BigDecimal = BigDecimal(amount)

  def purchaseDetailsInput: PurchaseDetailsInput = PurchaseDetailsInput(amount, currency.currencyCode)
}

object PurchaseDetails {
  implicit val format: OFormat[PurchaseDetails] = Json.format[PurchaseDetails]
}

case class CategoryQuantityOfGoods(category: String, quantity: String)

object CategoryQuantityOfGoods {
  implicit val format: OFormat[CategoryQuantityOfGoods] = Json.format[CategoryQuantityOfGoods]
}

case class AmountInPence(value: Long) {
  val inPounds: BigDecimal = (BigDecimal(value) / 100).setScale(2)
  val formattedInPounds: String = getCurrencyInstance(UK).format(inPounds)
}

object AmountInPence {
  implicit val format: Format[AmountInPence] = implicitly[Format[Long]].inmap(AmountInPence(_), _.value)
}

case class GoodsEntry(maybeCategoryQuantityOfGoods: Option[CategoryQuantityOfGoods] = None,
                      maybeGoodsVatRate: Option[GoodsVatRate] = None,
                      maybeCountryOfPurchase: Option[String] = None,
                      maybePurchaseDetails: Option[PurchaseDetails] = None) {

  val goodsIfComplete: Option[Goods] =
    for {
      categoryQuantityOfGoods <- maybeCategoryQuantityOfGoods
      goodsVatRate <- maybeGoodsVatRate
      countryOfPurchase <- maybeCountryOfPurchase
      priceOfGoods <- maybePurchaseDetails
    } yield Goods(categoryQuantityOfGoods, goodsVatRate, countryOfPurchase, priceOfGoods)
}

object GoodsEntry {
  implicit val format: OFormat[GoodsEntry] = Json.format[GoodsEntry]

  val empty: GoodsEntry = GoodsEntry()
}

case class GoodsEntries(entries: Seq[GoodsEntry] = Seq(GoodsEntry.empty)) {
  if (entries.isEmpty) throw new RuntimeException("GoodsEntries cannot be empty: use apply()")

  val declarationGoodsIfComplete: Option[DeclarationGoods] = {
    val goods = entries.flatMap(_.goodsIfComplete)

    if (entries.nonEmpty && (goods.size == entries.size)) Some(DeclarationGoods(goods))
    else None
  }

  val declarationGoodsComplete: Boolean = declarationGoodsIfComplete.isDefined

  def patch(idx: Int, goodsEntry: GoodsEntry): GoodsEntries =
    GoodsEntries(entries.updated(idx - 1, goodsEntry))

  def remove(idx: Int): GoodsEntries = {
    if (entries.size == 1) GoodsEntries.empty
    else GoodsEntries(entries.zipWithIndex.filter(_._2 != idx - 1).map(_._1))
  }
}

object GoodsEntries {
  implicit val format: OFormat[GoodsEntries] = Json.format[GoodsEntries]

  def apply(goodsEntry: GoodsEntry): GoodsEntries = GoodsEntries(Seq(goodsEntry))

  val empty: GoodsEntries = GoodsEntries()
}

case class Name(firstName: String, lastName: String) {
  override val toString: String = s"$firstName $lastName"
}

object Name {
  implicit val format: OFormat[Name] = Json.format[Name]
}

case class Email(email: String, confirmation: String)

object Email {
  implicit val format = Json.format[Email]
}

case class Eori(value: String) {
  override val toString: String = value
}

object Eori {
  implicit val format: OFormat[Eori] = Json.format[Eori]
}

case class JourneyDetailsEntry(placeOfArrival: Port, dateOfArrival: LocalDate)

object JourneyDetailsEntry {
  implicit val format: OFormat[JourneyDetailsEntry] = Json.format[JourneyDetailsEntry]
}

case class DeclarationJourney(sessionId: SessionId,
                              declarationType: DeclarationType,
                              createdAt: LocalDateTime = LocalDateTime.now(),
                              maybeExciseOrRestrictedGoods: Option[YesNo] = None,
                              maybeGoodsDestination: Option[GoodsDestination] = None,
                              maybeImportOrExportGoodsFromTheEUViaNorthernIreland: Option[YesNo] = None,
                              maybeValueWeightOfGoodsExceedsThreshold: Option[YesNo] = None,
                              goodsEntries: GoodsEntries = GoodsEntries.empty,
                              maybeNameOfPersonCarryingTheGoods: Option[Name] = None,
                              maybeEmailAddress: Option[Email] = None,
                              maybeIsACustomsAgent: Option[YesNo] = None,
                              maybeCustomsAgentName: Option[String] = None,
                              maybeCustomsAgentAddress: Option[Address] = None,
                              maybeEori: Option[Eori] = None,
                              maybeJourneyDetailsEntry: Option[JourneyDetailsEntry] = None,
                              maybeTravellingByVehicle: Option[YesNo] = None,
                              maybeTravellingBySmallVehicle: Option[YesNo] = None,
                              maybeRegistrationNumber: Option[String] = None) {

  val maybeCustomsAgent: Option[CustomsAgent] =
    for {
      _ <- maybeIsACustomsAgent
      customsAgentName <- maybeCustomsAgentName
      customsAgentAddress <- maybeCustomsAgentAddress
      if maybeIsACustomsAgent.exists(yn => YesNo.to(yn))
    } yield CustomsAgent(customsAgentName, customsAgentAddress)

  private val maybeCompleteJourneyDetails: Option[JourneyDetails] = maybeJourneyDetailsEntry.flatMap { journeyDetailsEntry =>
    (journeyDetailsEntry.placeOfArrival, maybeTravellingByVehicle, maybeTravellingBySmallVehicle, maybeRegistrationNumber) match {
      case (port: FootPassengerOnlyPort, _, _, _) =>
        Some(JourneyViaFootPassengerOnlyPort(port, journeyDetailsEntry.dateOfArrival))
      case (port: VehiclePort, Some(No), _, _) =>
        Some(JourneyOnFootViaVehiclePort(port, journeyDetailsEntry.dateOfArrival))
      case (port: VehiclePort, Some(Yes), Some(YesNo.Yes), Some(registrationNumber)) =>
        Some(JourneyInSmallVehicle(port, journeyDetailsEntry.dateOfArrival, registrationNumber))
      case _ => None
    }
  }

  val declarationIfRequiredAndComplete: Option[Declaration] = {
    val discardedAnswersAreCompleteAndRequireADeclaration =
      maybeGoodsDestination.isDefined &&
        maybeImportOrExportGoodsFromTheEUViaNorthernIreland.contains(No) &&
        maybeExciseOrRestrictedGoods.contains(No) &&
        maybeValueWeightOfGoodsExceedsThreshold.contains(No) &&
        maybeCustomsAgent.isDefined || maybeIsACustomsAgent.contains(No)

    for {
      goodsDestination <- maybeGoodsDestination
      goods <- goodsEntries.declarationGoodsIfComplete
      nameOfPersonCarryingTheGoods <- maybeNameOfPersonCarryingTheGoods
      email <- maybeEmailAddress
      eori <- maybeEori
      journeyDetails <- maybeCompleteJourneyDetails

      if discardedAnswersAreCompleteAndRequireADeclaration
    } yield {
      Declaration(sessionId, declarationType, goodsDestination, goods, nameOfPersonCarryingTheGoods, email, maybeCustomsAgent, eori, journeyDetails)
    }
  }

  val declarationRequiredAndComplete: Boolean = declarationIfRequiredAndComplete.isDefined
}

object DeclarationJourney {
  implicit val format: OFormat[DeclarationJourney] = Json.format[DeclarationJourney]

  val id = "sessionId"
}

case class Goods(categoryQuantityOfGoods: CategoryQuantityOfGoods,
                 goodsVatRate: GoodsVatRate,
                 countryOfPurchase: String,
                 purchaseDetails: PurchaseDetails) {

  val calculationRequest: CalculationRequest =
    CalculationRequest(purchaseDetails.numericAmount, purchaseDetails.currency.currencyCode, goodsVatRate)
}

object Goods {
  implicit val format: OFormat[Goods] = Json.format[Goods]
}

case class DeclarationGoods(goods: Seq[Goods])

object DeclarationGoods {
  implicit val format: OFormat[DeclarationGoods] = Json.format[DeclarationGoods]

  def apply(goods: Goods): DeclarationGoods = DeclarationGoods(Seq(goods))
}

case class CustomsAgent(name: String, address: Address)

object CustomsAgent {
  implicit val format: OFormat[CustomsAgent] = Json.format[CustomsAgent]
}

sealed trait YesNo extends EnumEntry {
  val messageKey = s"${YesNo.baseMessageKey}.${entryName.toLowerCase}"
}

object YesNo extends Enum[YesNo] {
  override val baseMessageKey: String = "site"
  override val values: immutable.IndexedSeq[YesNo] = findValues

  def from(bool: Boolean): YesNo = if (bool) Yes else No

  def to(yesNo: YesNo): Boolean = yesNo match {
    case Yes => true
    case No => false
  }

  case object Yes extends YesNo

  case object No extends YesNo

}

sealed trait JourneyDetails {
  val placeOfArrival: Port
  val dateOfArrival: LocalDate
  val formattedDateOfArrival: String = DateTimeFormatter.ofPattern("dd MMM yyyy").format(dateOfArrival)
  val travellingByVehicle: YesNo = No
  val maybeRegistrationNumber: Option[String] = None
}

case class JourneyViaFootPassengerOnlyPort(placeOfArrival: FootPassengerOnlyPort, dateOfArrival: LocalDate) extends JourneyDetails

case class JourneyOnFootViaVehiclePort(placeOfArrival: VehiclePort, dateOfArrival: LocalDate) extends JourneyDetails

case class JourneyInSmallVehicle(placeOfArrival: VehiclePort, dateOfArrival: LocalDate, registrationNumber: String) extends JourneyDetails {
  override val travellingByVehicle: YesNo = Yes
  override val maybeRegistrationNumber: Option[String] = Some(registrationNumber)
}

object JourneyDetails {
  implicit val format: OFormat[JourneyDetails] = Json.format[JourneyDetails]
}

object JourneyViaFootPassengerOnlyPort {
  implicit val format: OFormat[JourneyViaFootPassengerOnlyPort] = Json.format[JourneyViaFootPassengerOnlyPort]
}

object JourneyOnFootViaVehiclePort {
  implicit val format: OFormat[JourneyOnFootViaVehiclePort] = Json.format[JourneyOnFootViaVehiclePort]
}

object JourneyInSmallVehicle {
  implicit val format: OFormat[JourneyInSmallVehicle] = Json.format[JourneyInSmallVehicle]
}

case class Declaration(sessionId: SessionId,
                       declarationType: DeclarationType,
                       goodsDestination: GoodsDestination,
                       declarationGoods: DeclarationGoods,
                       nameOfPersonCarryingTheGoods: Name,
                       email: Email,
                       maybeCustomsAgent: Option[CustomsAgent],
                       eori: Eori,
                       journeyDetails: JourneyDetails,
                       dateOfDeclaration: LocalDateTime = LocalDateTime.now,
                       mibReference: MibReference = Declaration.mibReference
                      )

object Declaration extends MibReferenceGenerator {
  implicit val format: OFormat[Declaration] = Json.format[Declaration]
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM YYYY, h:mm a", Locale.ENGLISH)

  implicit class DeclarationDateTime(dateOfDeclaration: LocalDateTime) {
    def formattedDate: String = {
      dateOfDeclaration.format(formatter)
    }
  }
}

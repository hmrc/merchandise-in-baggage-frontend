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

package uk.gov.hmrc.merchandiseinbaggage.model.core

import enumeratum.EnumEntry
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.merchandiseinbaggage.model.Enum
import uk.gov.hmrc.merchandiseinbaggage.model.adresslookup.Address
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.calculation.CalculationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.service.PortService
import uk.gov.hmrc.merchandiseinbaggage.utils.Obfuscator.{maybeObfuscate, obfuscate}

import java.text.NumberFormat.getCurrencyInstance
import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import java.util.Locale.UK
import java.util.UUID
import java.util.UUID.randomUUID
import scala.collection.immutable

case class SessionId(value: String)

object SessionId {
  implicit val format: Format[SessionId] = implicitly[Format[String]].inmap(SessionId(_), _.value)

  def apply(): SessionId = SessionId(randomUUID().toString)
}


case class CategoryQuantityOfGoods(category: String, quantity: String)

object CategoryQuantityOfGoods {
  implicit val format: OFormat[CategoryQuantityOfGoods] = Json.format[CategoryQuantityOfGoods]
}

case class AmountInPence(value: Long) {
  val inPounds: BigDecimal = (BigDecimal(value) / 100).setScale(2)
  val formattedInPounds: String = getCurrencyInstance(UK).format(inPounds)
  val formattedInPoundsUI: String = formattedInPounds.split("\\.00")(0) //TODO not sure if necessary different formats
}

object AmountInPence {
  implicit val format: Format[AmountInPence] = implicitly[Format[Long]].inmap(AmountInPence(_), _.value)

  def fromBigDecimal(in: BigDecimal): AmountInPence = AmountInPence((in * 100).toLong)
}

case class GoodsEntry(maybeCategoryQuantityOfGoods: Option[CategoryQuantityOfGoods] = None,
                      maybeGoodsVatRate: Option[GoodsVatRate] = None,
                      maybeCountryOfPurchase: Option[Country] = None,
                      maybePurchaseDetails: Option[PurchaseDetails] = None) {
  val goodsIfComplete: Option[Goods] =
    for {
      categoryQuantityOfGoods <- maybeCategoryQuantityOfGoods
      goodsVatRate <- maybeGoodsVatRate
      countryOfPurchase <- maybeCountryOfPurchase
      priceOfGoods <- maybePurchaseDetails
    } yield Goods(categoryQuantityOfGoods, goodsVatRate, countryOfPurchase, priceOfGoods)

  val isComplete: Boolean = goodsIfComplete.isDefined
}

object GoodsEntry {
  implicit val format: OFormat[GoodsEntry] = Json.format[GoodsEntry]

  val empty: GoodsEntry = GoodsEntry()
}

case class GoodsEntries(entries: Seq[GoodsEntry] = Seq(GoodsEntry.empty)) {
  if (entries.isEmpty) throw new RuntimeException("GoodsEntries cannot be empty: use apply()")

  val declarationGoodsIfComplete: Option[DeclarationGoods] = {
    val goods = entries.flatMap(_.goodsIfComplete)

    if (goods.nonEmpty) Some(DeclarationGoods(goods))
    else None
  }

  val declarationGoodsComplete: Boolean = declarationGoodsIfComplete.isDefined

  def addEmptyIfNecessary(): GoodsEntries =
    if (entries.lastOption.fold(true)(_.isComplete)) GoodsEntries(entries :+ GoodsEntry.empty)
    else this

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

  lazy val obfuscated: Name = Name(obfuscate(firstName), obfuscate(lastName))
}

object Name {
  implicit val format: OFormat[Name] = Json.format[Name]
}

case class Email(email: String, confirmation: String) {
  lazy val obfuscated: Email = Email(obfuscate(email), obfuscate(confirmation))
}

object Email {
  implicit val format: OFormat[Email] = Json.format[Email]
}

case class Eori(value: String) {
  override val toString: String = value

  lazy val obfuscated: Eori = Eori(obfuscate(value))
}

object Eori {
  implicit val format: OFormat[Eori] = Json.format[Eori]
}

case class JourneyDetailsEntry(portCode: String, dateOfTravel: LocalDate)

object JourneyDetailsEntry {
  implicit val format: OFormat[JourneyDetailsEntry] = Json.format[JourneyDetailsEntry]
}

case class DeclarationJourney(sessionId: SessionId,
                              declarationType: DeclarationType,
                              createdAt: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
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
                              maybeRegistrationNumber: Option[String] = None,
                              declarationId: DeclarationId = DeclarationId(UUID.randomUUID().toString)
                             ) {
  lazy val obfuscated: DeclarationJourney =
    this.copy(
      maybeNameOfPersonCarryingTheGoods = maybeNameOfPersonCarryingTheGoods.map(_.obfuscated),
      maybeEmailAddress = maybeEmailAddress.map(_.obfuscated),
      maybeCustomsAgentName = maybeObfuscate(maybeCustomsAgentName),
      maybeCustomsAgentAddress = maybeCustomsAgentAddress.map(_.obfuscated),
      maybeEori = maybeEori.map(_.obfuscated),
      maybeRegistrationNumber = maybeObfuscate(maybeRegistrationNumber)
    )

  val maybeCustomsAgent: Option[CustomsAgent] =
    for {
      _ <- maybeIsACustomsAgent
      customsAgentName <- maybeCustomsAgentName
      customsAgentAddress <- maybeCustomsAgentAddress
      if maybeIsACustomsAgent.exists(yn => YesNo.to(yn))
    } yield CustomsAgent(customsAgentName, customsAgentAddress)

  private val maybeCompleteJourneyDetails: Option[JourneyDetails] = maybeJourneyDetailsEntry.flatMap { journeyDetailsEntry =>
    val maybePort = PortService.getPortByCode(journeyDetailsEntry.portCode)
    (maybePort, maybeTravellingByVehicle, maybeTravellingBySmallVehicle, maybeRegistrationNumber) match {
      case (Some(port), Some(No), _, _) =>
        Some(JourneyOnFoot(port, journeyDetailsEntry.dateOfTravel))
      case (Some(port), Some(Yes), Some(Yes), Some(registrationNumber)) =>
        Some(JourneyInSmallVehicle(port, journeyDetailsEntry.dateOfTravel, registrationNumber))
      case _ => None
    }
  }

  val declarationIfRequiredAndComplete: Option[Declaration] = {
    val ultimateSourceOrDestinationIsDefinedAndNotTheEUViaNorthernIreland: Boolean =
      (maybeGoodsDestination, maybeImportOrExportGoodsFromTheEUViaNorthernIreland) match {
        case (Some(GreatBritain), _) => true
        case (Some(NorthernIreland), Some(No)) => true
        case _ => false
      }

    val discardedAnswersAreCompleteAndRequireADeclaration =
      ultimateSourceOrDestinationIsDefinedAndNotTheEUViaNorthernIreland &&
        maybeExciseOrRestrictedGoods.contains(No) &&
        maybeValueWeightOfGoodsExceedsThreshold.contains(No) &&
        (maybeCustomsAgent.isDefined || maybeIsACustomsAgent.contains(No))

    for {
      goodsDestination <- maybeGoodsDestination
      goods <- goodsEntries.declarationGoodsIfComplete
      nameOfPersonCarryingTheGoods <- maybeNameOfPersonCarryingTheGoods
      email <- maybeEmailAddress
      eori <- maybeEori
      journeyDetails <- maybeCompleteJourneyDetails

      if discardedAnswersAreCompleteAndRequireADeclaration
    } yield {
      Declaration(declarationId, sessionId, declarationType, goodsDestination, goods, nameOfPersonCarryingTheGoods, email, maybeCustomsAgent, eori, journeyDetails)
    }
  }

  val declarationRequiredAndComplete: Boolean = declarationIfRequiredAndComplete.isDefined
}

object DeclarationJourney extends MongoDateTimeFormats {
  implicit val format: OFormat[DeclarationJourney] = Json.format[DeclarationJourney]

  val id = "sessionId"
}

case class Goods(categoryQuantityOfGoods: CategoryQuantityOfGoods,
                 goodsVatRate: GoodsVatRate,
                 countryOfPurchase: Country,
                 purchaseDetails: PurchaseDetails) {

  val calculationRequest: CalculationRequest =
    CalculationRequest(purchaseDetails.numericAmount, purchaseDetails.currency.code, goodsVatRate)
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



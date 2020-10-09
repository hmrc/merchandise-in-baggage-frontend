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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID.randomUUID

import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, SummaryList, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.Currency

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

case class GoodsEntry(maybeCategoryQuantityOfGoods: Option[CategoryQuantityOfGoods] = None,
                      maybeGoodsVatRate: Option[GoodsVatRate] = None,
                      maybeCountryOfPurchase: Option[String] = None,
                      maybePurchaseDetails: Option[PurchaseDetails] = None,
                      maybeInvoiceNumber: Option[String] = None,
                      maybeTaxDue: Option[BigDecimal] = None) {

  val goodsIfComplete: Option[Goods] =
    for {
      categoryQuantityOfGoods <- maybeCategoryQuantityOfGoods
      goodsVatRate <- maybeGoodsVatRate
      countryOfPurchase <- maybeCountryOfPurchase
      priceOfGoods <- maybePurchaseDetails
      invoiceNumber <- maybeInvoiceNumber
      taxDue <- maybeTaxDue
    } yield Goods(categoryQuantityOfGoods, goodsVatRate, countryOfPurchase, priceOfGoods, invoiceNumber, taxDue)
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

case class Address(maybeLine1: Option[String],
                   maybeLine2: Option[String] = None,
                   maybeTown: Option[String] = None,
                   maybeCounty: Option[String] = None,
                   postCode: String) {
  val populatedAddressLines: Seq[String] = Seq(maybeLine1, maybeLine2, maybeTown, maybeCounty).flatten
}

object Address {
  implicit val format: OFormat[Address] = Json.format[Address]

  def apply(line1: String, line2: String, postcode: String): Address = Address(Some(line1), Some(line2), None, None, postcode)
}

case class Eori(value: String) {
  override val toString: String = value
}

object Eori {
  implicit val format: OFormat[Eori] = Json.format[Eori]
}

case class JourneyDetails(placeOfArrival: Port, dateOfArrival: LocalDate) {
  val formattedDateOfArrival: String = DateTimeFormatter.ofPattern("dd MMM yyyy").format(dateOfArrival)
}

object JourneyDetails {
  implicit val format: OFormat[JourneyDetails] = Json.format[JourneyDetails]
}

case class DeclarationJourney(sessionId: SessionId,
                              maybeExciseOrRestrictedGoods: Option[Boolean] = None,
                              maybeGoodsDestination: Option[GoodsDestination] = None,
                              maybeValueWeightOfGoodsExceedsThreshold: Option[Boolean] = None,
                              goodsEntries: GoodsEntries = GoodsEntries.empty,
                              maybeNameOfPersonCarryingTheGoods: Option[Name] = None,
                              maybeIsACustomsAgent: Option[Boolean] = None,
                              maybeCustomsAgentName: Option[String] = None,
                              maybeCustomsAgentAddress: Option[Address] = None,
                              maybeEori: Option[Eori] = None,
                              maybeJourneyDetails: Option[JourneyDetails] = None,
                              maybeTravellingByVehicle: Option[Boolean] = None,
                              maybeTravellingBySmallVehicle: Option[Boolean] = None,
                              maybeRegistrationNumber: Option[String] = None) {

  val maybeCustomsAgent: Option[CustomsAgent] =
    if (maybeIsACustomsAgent.getOrElse(false)) {
      for {
        customsAgentName <- maybeCustomsAgentName
        customsAgentAddress <- maybeCustomsAgentAddress
      } yield CustomsAgent(customsAgentName, customsAgentAddress)
    } else None

  val journeyDetailsCompleteAndDeclarationRequired: Boolean =
    maybeJourneyDetails.fold(false) { journeyDetails =>
      if (journeyDetails.placeOfArrival.rollOnRollOff) {
        if (maybeTravellingByVehicle.contains(false)) {
          true
        } else {
          if (maybeTravellingByVehicle.isEmpty) false
          else if (!maybeTravellingBySmallVehicle.getOrElse(false)) false
          else maybeRegistrationNumber.fold(false)(_ => true)
        }
      } else true
    }

  val declarationIfRequiredAndComplete: Option[Declaration] = {
    val discardedAnswersAreCompleteAndRequireADeclaration =
      maybeGoodsDestination.isDefined &&
        maybeExciseOrRestrictedGoods.contains(false) &&
        maybeValueWeightOfGoodsExceedsThreshold.contains(false) &&
        journeyDetailsCompleteAndDeclarationRequired &&
        (maybeCustomsAgent.isDefined || maybeIsACustomsAgent.contains(false))

    for {
      nameOfPersonCarryingTheGoods <- maybeNameOfPersonCarryingTheGoods
      eori <- maybeEori
      journeyDetails <- maybeJourneyDetails
      goods <- goodsEntries.declarationGoodsIfComplete

      if discardedAnswersAreCompleteAndRequireADeclaration
    } yield {
      Declaration(
        sessionId,
        goods,
        nameOfPersonCarryingTheGoods,
        maybeCustomsAgent,
        eori,
        journeyDetails,
        maybeTravellingByVehicle.getOrElse(false),
        maybeRegistrationNumber
      )
    }
  }
}

object DeclarationJourney {
  implicit val format: OFormat[DeclarationJourney] = Json.format[DeclarationJourney]

  val id = "sessionId"
}

case class Goods(categoryQuantityOfGoods: CategoryQuantityOfGoods,
                 goodsVatRate: GoodsVatRate,
                 countryOfPurchase: String,
                 purchaseDetails: PurchaseDetails,
                 invoiceNumber: String,
                 taxDue: BigDecimal) {
  def toSummaryList(implicit messages: Messages): SummaryList = {
    val price =
      s"${purchaseDetails.amount}, ${purchaseDetails.currency.displayName})"

    SummaryList(Seq(
      SummaryListRow(
        Key(Text(messages("reviewGoods.list.item"))),
        Value(Text(categoryQuantityOfGoods.category))
      ),
      SummaryListRow(
        Key(Text(messages("reviewGoods.list.quantity"))),
        Value(Text(categoryQuantityOfGoods.quantity))
      ),
      SummaryListRow(
        Key(Text(messages("reviewGoods.list.country"))),
        Value(Text(countryOfPurchase))
      ),
      SummaryListRow(
        Key(Text(messages("reviewGoods.list.price"))),
        Value(Text(price))
      ),
      SummaryListRow(
        Key(Text(messages("reviewGoods.list.invoice"))),
        Value(Text(invoiceNumber))
      )
    ))
  }
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

case class Declaration(sessionId: SessionId,
                       declarationGoods: DeclarationGoods,
                       nameOfPersonCarryingTheGoods: Name,
                       maybeCustomsAgent: Option[CustomsAgent],
                       eori: Eori,
                       journeyDetails: JourneyDetails,
                       travellingByVehicle: Boolean,
                       maybeRegistrationNumber: Option[String] = None)

object Declaration {
  implicit val format: OFormat[Declaration] = Json.format[Declaration]
}

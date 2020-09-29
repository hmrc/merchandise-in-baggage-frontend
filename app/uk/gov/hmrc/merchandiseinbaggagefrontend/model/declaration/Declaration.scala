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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration

import java.time.LocalDate
import java.util.UUID.randomUUID

import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestination
import uk.gov.hmrc.merchandiseinbaggagefrontend.utils.ValueClassFormat

case class SessionId(value: String)

object SessionId {
  implicit val format: Format[SessionId] = ValueClassFormat.format(value => SessionId.apply(value))(_.value)

  def apply(): SessionId = SessionId(randomUUID().toString)
}

case class CurrencyAmount(value: BigDecimal) {
  override val toString = s"${value.setScale(2)}"
}

object CurrencyAmount {
  implicit val format: OFormat[CurrencyAmount] = Json.format[CurrencyAmount]
}

case class PriceOfGoods(amount: CurrencyAmount, currency: String) {
  override val toString = s"$amount $currency"
}

object PriceOfGoods {
  implicit val format: OFormat[PriceOfGoods] = Json.format[PriceOfGoods]
}

case class GoodsEntry(typeOfGoods: String,
                      maybeCountryOfPurchase: Option[String] = None,
                      maybePriceOfGoods: Option[PriceOfGoods] = None,
                      maybeTaxDue: Option[CurrencyAmount] = None)

object GoodsEntry {
  implicit val format: OFormat[GoodsEntry] = Json.format[GoodsEntry]
}

case class Name(firstName: String, secondName: String) {
  override val toString: String = s"$firstName $secondName"
}

object Name {
  implicit val format: OFormat[Name] = Json.format[Name]
}

case class Address(maybeLine1: Option[String],
                   maybeLine2: Option[String] = None,
                   maybeTown: Option[String] = None,
                   maybeCounty: Option[String] = None,
                   postCode: String) {

  override val toString: String = {
    def lineString(maybeLine: Option[String]) = maybeLine.fold("")(line => s"$line, ")

    s"${lineString(maybeLine1)}${lineString(maybeLine2)}${lineString(maybeTown)}${lineString(maybeCounty)}$postCode"
  }
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

case class JourneyDetails(placeOfArrival: String, dateOfArrival: LocalDate)

object JourneyDetails {
  implicit val format: OFormat[JourneyDetails] = Json.format[JourneyDetails]
}

case class DeclarationJourney(sessionId: SessionId,
                              maybeExciseOrRestrictedGoods: Option[Boolean] = None,
                              goodsEntries: Seq[GoodsEntry] = Seq.empty,
                              maybeName: Option[Name] = None,
                              maybeAddress: Option[Address] = None,
                              maybeEori: Option[Eori] = None,
                              maybeJourneyDetails: Option[JourneyDetails] = None,
                              maybeGoodsDestination: Option[GoodsDestination] = None,
                              maybeValueWeightOfGoods: Option[Boolean] = None)

object DeclarationJourney {
  implicit val format: OFormat[DeclarationJourney] = Json.format[DeclarationJourney]

  val id = "sessionId"
}

case class Goods(typeOfGoods: String, countryOfPurchase: String, priceOfGoods: PriceOfGoods, taxDue: CurrencyAmount)

object Goods {
  implicit val format: OFormat[Goods] = Json.format[Goods]

  def apply(goodsEntry: GoodsEntry): Goods = (
    for {
      countryOfPurchase <- goodsEntry.maybeCountryOfPurchase
      priceOfGoods <- goodsEntry.maybePriceOfGoods
      taxDue <- goodsEntry.maybeTaxDue
    } yield Goods(goodsEntry.typeOfGoods, countryOfPurchase, priceOfGoods, taxDue)
    ).getOrElse(throw new RuntimeException(s"incomplete goods entry: [$goodsEntry]"))
}

case class Declaration(sessionId: SessionId,
                       goods: Seq[Goods],
                       name: Name,
                       address: Address,
                       eori: Eori,
                       journeyDetails: JourneyDetails)

object Declaration {
  implicit val format: OFormat[Declaration] = Json.format[Declaration]

  def apply(journey: DeclarationJourney): Declaration = (
    for {
      name <- journey.maybeName
      address <- journey.maybeAddress
      eori <- journey.maybeEori
      journeyDetails <- journey.maybeJourneyDetails
    } yield
      Declaration(
        journey.sessionId,
        journey.goodsEntries.map(entry => Goods(entry)),
        name,
        address,
        eori,
        journeyDetails
      )
    ).getOrElse(throw new RuntimeException(s"incomplete declaration journey: [$journey]"))
}

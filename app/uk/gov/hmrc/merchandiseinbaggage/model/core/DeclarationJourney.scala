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

package uk.gov.hmrc.merchandiseinbaggage.model.core

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.Address
import uk.gov.hmrc.merchandiseinbaggage.service.{MibReferenceGenerator, PortService}

case class GoodsEntries(entries: Seq[GoodsEntry]) {
  if (entries.isEmpty) throw new RuntimeException("GoodsEntries cannot be empty: use apply()")

  val declarationGoodsIfComplete: Option[DeclarationGoods] = {
    val goods = entries.flatMap(_.goodsIfComplete)

    if (goods.nonEmpty) Some(DeclarationGoods(goods))
    else None
  }

  val declarationGoodsComplete: Boolean = declarationGoodsIfComplete.isDefined

  def addEmptyIfNecessary(): GoodsEntries =
    if (entries.lastOption.fold(true)(_.isComplete)) entries.head match {
      case _: ImportGoodsEntry => GoodsEntries(entries :+ ImportGoodsEntry())
      case _: ExportGoodsEntry => GoodsEntries(entries :+ ExportGoodsEntry())
    } else this

  def patch(idx: Int, goodsEntry: GoodsEntry): GoodsEntries =
    GoodsEntries(entries.updated(idx - 1, goodsEntry))

  def remove(idx: Int): GoodsEntries =
    if (entries.size == 1) entries.head match {
      case _: ImportGoodsEntry => GoodsEntries(ImportGoodsEntry())
      case _: ExportGoodsEntry => GoodsEntries(ExportGoodsEntry())
    } else GoodsEntries(entries.zipWithIndex.filter(_._2 != idx - 1).map(_._1))
}

object GoodsEntries {
  implicit val format: OFormat[GoodsEntries] = Json.format[GoodsEntries]

  def apply(goodsEntry: GoodsEntry): GoodsEntries = GoodsEntries(Seq(goodsEntry))
}

case class DeclarationJourney(
  sessionId: SessionId,
  declarationType: DeclarationType,
  journeyType: JourneyType = New,
  createdAt: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
  maybeExciseOrRestrictedGoods: Option[YesNo] = None,
  maybeGoodsDestination: Option[GoodsDestination] = None,
  maybeValueWeightOfGoodsBelowThreshold: Option[YesNo] = None,
  goodsEntries: GoodsEntries,
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
  maybeRetrieveDeclaration: Option[RetrieveDeclaration] = None,
  declarationId: DeclarationId = DeclarationId(UUID.randomUUID().toString))
    extends MibReferenceGenerator {

  val maybeCustomsAgent: Option[CustomsAgent] =
    for {
      _                   <- maybeIsACustomsAgent
      customsAgentName    <- maybeCustomsAgentName
      customsAgentAddress <- maybeCustomsAgentAddress
      if maybeIsACustomsAgent.exists(yn => YesNo.to(yn))
    } yield CustomsAgent(customsAgentName, customsAgentAddress)

  val maybeCompleteJourneyDetails: Option[JourneyDetails] = maybeJourneyDetailsEntry.flatMap { journeyDetailsEntry =>
    val maybePort = PortService.getPortByCode(journeyDetailsEntry.portCode)
    (maybePort, maybeTravellingByVehicle, maybeTravellingBySmallVehicle, maybeRegistrationNumber) match {
      case (Some(port), Some(No), _, _) =>
        Some(JourneyOnFoot(port, journeyDetailsEntry.dateOfTravel))
      case (Some(port), Some(Yes), Some(Yes), Some(registrationNumber)) =>
        Some(JourneyInSmallVehicle(port, journeyDetailsEntry.dateOfTravel, registrationNumber))
      case _ => None
    }
  }

  val declarationIfRequiredAndComplete: Option[Declaration] = journeyType match {
    case Amend => None
    case New =>
      val discardedAnswersAreCompleteAndRequireADeclaration =
        maybeGoodsDestination.contains(GreatBritain) &&
          maybeExciseOrRestrictedGoods.contains(No) &&
          maybeValueWeightOfGoodsBelowThreshold.contains(Yes) &&
          (maybeCustomsAgent.isDefined || maybeIsACustomsAgent.contains(No))

      for {
        goodsDestination             <- maybeGoodsDestination
        goods                        <- goodsEntries.declarationGoodsIfComplete
        nameOfPersonCarryingTheGoods <- maybeNameOfPersonCarryingTheGoods
        email                        <- maybeEmailAddress
        eori                         <- maybeEori
        journeyDetails               <- maybeCompleteJourneyDetails
        if discardedAnswersAreCompleteAndRequireADeclaration
      } yield {
        Declaration(
          declarationId,
          sessionId,
          declarationType,
          goodsDestination,
          goods,
          nameOfPersonCarryingTheGoods,
          Some(email),
          maybeCustomsAgent,
          eori,
          journeyDetails,
          LocalDateTime.now(),
          mibReference
        )
      }
  }

  val declarationRequiredAndComplete: Boolean = declarationIfRequiredAndComplete.isDefined

  val amendmentIfRequiredAndComplete: Option[Amendment] = journeyType match {
    case New   => None
    case Amend => goodsEntries.declarationGoodsIfComplete.map(goods => Amendment(1, LocalDateTime.now, goods))
  }

  val amendmentRequiredAndComplete: Boolean = amendmentIfRequiredAndComplete.isDefined
}

object DeclarationJourney extends MongoDateTimeFormats {
  implicit val format: OFormat[DeclarationJourney] = Json.format[DeclarationJourney]

  def apply(sessionId: SessionId, declarationType: DeclarationType): DeclarationJourney = declarationType match {
    case Import =>
      DeclarationJourney(
        sessionId = sessionId,
        declarationType = declarationType,
        goodsEntries = GoodsEntries(ImportGoodsEntry())
      )
    case Export =>
      DeclarationJourney(
        sessionId = sessionId,
        declarationType = declarationType,
        goodsEntries = GoodsEntries(ExportGoodsEntry())
      )
  }

  val id = "sessionId"

  implicit class UpdateGoodsEntries(declarationJourney: DeclarationJourney) {
    def updateGoodsEntries(): DeclarationJourney =
      declarationJourney.copy(goodsEntries = declarationJourney.goodsEntries.addEmptyIfNecessary())
  }
}

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

package uk.gov.hmrc.merchandiseinbaggage.model.core

import play.api.libs.json._
import uk.gov.hmrc.merchandiseinbaggage.config.IsAssistedDigitalConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.Address
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney._
import uk.gov.hmrc.merchandiseinbaggage.service.{MibReferenceGenerator, PortService}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDateTime, ZoneOffset, ZonedDateTime}
import java.util.UUID
import scala.util._

case class GoodsEntries(entries: Seq[GoodsEntry]) {
  if (entries.isEmpty) throw new RuntimeException("GoodsEntries cannot be empty: use apply()")

  val declarationGoodsIfComplete: Option[DeclarationGoods] = {
    val goods: Seq[Goods] = entries.flatMap(_.goodsIfComplete)

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
  createdAt: LocalDateTime = LocalDateTime
    .now(ZoneOffset.UTC)
    .truncatedTo(ChronoUnit.MILLIS),
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
    extends MibReferenceGenerator with IsAssistedDigitalConfiguration {

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
        email                        <- defaultEmail(isAssistedDigital, maybeEmailAddress)
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
          userEmail(isAssistedDigital, maybeEmailAddress, email),
          maybeCustomsAgent,
          eori,
          journeyDetails,
          LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
          mibReference
        )
      }
  }

  val declarationRequiredAndComplete: Boolean = declarationIfRequiredAndComplete.isDefined

  val amendmentIfRequiredAndComplete: Option[Amendment] = journeyType match {
    case New => None
    case Amend =>
      goodsEntries.declarationGoodsIfComplete.map(goods => Amendment(1, LocalDateTime.now.truncatedTo(ChronoUnit.MILLIS), goods))
  }

  val amendmentRequiredAndComplete: Boolean = amendmentIfRequiredAndComplete.isDefined
}

object DeclarationJourney {

  def parseDateString(dateValue: String): JsResult[LocalDateTime] = {
    val parsedTime: Try[LocalDateTime] = Try {
      if (dateValue.contains("Z")) {
        ZonedDateTime.parse(dateValue).toLocalDateTime
      } else {
        LocalDateTime.parse(dateValue)
      }
    }
    parsedTime match {
      case Failure(_)     => JsError("Unexpected LocalDateTime Format")
      case Success(value) => JsSuccess(value)
    }
  }

  def parseBigDecimal(bigDecimal: BigDecimal): JsResult[LocalDateTime] =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(bigDecimal.toLong), ZoneOffset.UTC) match {
      case d: LocalDateTime => JsSuccess(d)
      case _                => JsError("Unexpected LocalDateTime Format")
    }

  implicit val localDateTimeRead: Reads[LocalDateTime] = {
    case JsObject(map) if map.contains("$date") =>
      map("$date") match {
        case JsNumber(bigDecimal) => parseBigDecimal(bigDecimal)
        case JsObject(stringObject) if (stringObject.contains("$numberLong")) =>
          val extractedBigDecimal: BigDecimal = BigDecimal(stringObject("$numberLong").as[JsString].value)
          parseBigDecimal(extractedBigDecimal)
        case JsString(dateValue) =>
          parseDateString(dateValue)
        case _ => JsError("Unexpected LocalDateTime Format")
      }
    case JsString(dateValue) =>
      parseDateString(dateValue)
    case _ => JsError("Unexpected LocalDateTime Format")
  }

  implicit val dateFormat: Format[LocalDateTime] = Format(localDateTimeRead, MongoJavatimeFormats.localDateTimeWrites)
  implicit val format: OFormat[DeclarationJourney] = Json.format[DeclarationJourney]

  def apply(sessionId: SessionId, declarationType: DeclarationType, journeyType: JourneyType): DeclarationJourney =
    DeclarationJourney(
      sessionId = sessionId,
      declarationType = declarationType,
      journeyType = journeyType,
      goodsEntries = goodsEntries(declarationType)
    )

  private def goodsEntries(declarationType: DeclarationType) =
    declarationType match {
      case Import => GoodsEntries(ImportGoodsEntry())
      case Export => GoodsEntries(ExportGoodsEntry())
    }

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

  def defaultEmail(isAssistedDigital: Boolean, maybeEmailAddress: Option[Email]): Option[Email] =
    if (isAssistedDigital) Some(Email("")) else maybeEmailAddress

  def userEmail(isAssistedDigital: Boolean, maybeEmailAddress: Option[Email], email: Email) =
    if (isAssistedDigital) maybeEmailAddress else Some(email)

  implicit class UpdateGoodsEntries(declarationJourney: DeclarationJourney) {
    def updateGoodsEntries(): DeclarationJourney =
      declarationJourney.copy(goodsEntries = declarationJourney.goodsEntries.addEmptyIfNecessary())
  }

}

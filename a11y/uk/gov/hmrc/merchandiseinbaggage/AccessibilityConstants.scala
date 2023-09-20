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
package uk.gov.hmrc.merchandiseinbaggage

import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration.findSource
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsVatRates.Twenty
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, Currency, CustomsAgent, Declaration, DeclarationGoods, DeclarationId, DeclarationType, Email, Eori, GoodsDestination, GoodsDestinations, ImportGoods, JourneyDetails, JourneyDetailsEntry, JourneyInSmallVehicle, MibReference, Name, PaymentStatus, Port, PurchaseDetails, SessionId, TotalCalculationResult, YesNoDontKnow}

import java.time.{LocalDate, LocalDateTime}

trait AccessibilityConstants {
  val aPurchaseDetails: PurchaseDetails =
    PurchaseDetails("199.99", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European")))
  val aCategoryOfGoods: String = "test good"
  val aGoods: ImportGoods               = ImportGoods(aCategoryOfGoods, Twenty, YesNoDontKnow.Yes, aPurchaseDetails)
  private val aName: Name = Name("first", "last")
  val aDeclarationGood: DeclarationGoods             = DeclarationGoods(Seq(aGoods))
  val aDateOfDeclaration = LocalDateTime.now()
  val journeyDate: LocalDate = LocalDate.now
  val journeyPort = Port("DVR", "title.dover", isGB = true, List("Port of Dover"))
  private val journeyInSmallVehicle: JourneyInSmallVehicle = JourneyInSmallVehicle(
    journeyPort,
    journeyDate,
    "Lx123"
  )

  val declaration: Declaration = Declaration(
    declarationId = DeclarationId("1234"),
    sessionId = SessionId("sessionId"),
    declarationType = DeclarationType.Import,
    goodsDestination = GoodsDestinations.GreatBritain,
    declarationGoods = aDeclarationGood,
    nameOfPersonCarryingTheGoods = aName,
    email = None,
    maybeCustomsAgent = None,
    eori = Eori("ZZ12345678"),
    journeyDetails = journeyInSmallVehicle,
    dateOfDeclaration = aDateOfDeclaration,
    mibReference = MibReference("mibRef"),
    maybeTotalCalculationResult = None,
    paymentStatus = None,
    source = None,
    amendments = Seq.empty
  )
}

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

import java.time.LocalDateTime
import java.util.UUID

import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration._
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.{Address, AddressLookupCountry}
import uk.gov.hmrc.merchandiseinbaggage.utils.DateUtils._
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.utils.Obfuscate._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

class DeclarationSpec extends BaseSpecWithApplication with CoreTestData {
  private val completedNonCustomsAgentJourney = completedDeclarationJourney.copy(maybeIsACustomsAgent = Some(No))
  private val incompleteGoodsEntry = completedImportGoods.copy(maybePurchaseDetails = None)
  private val incompleteGoodEntries = GoodsEntries(Seq(incompleteGoodsEntry))
  private val vehicleRegistrationNumber = "reg"

  "PurchaseDetails toString" should {
    "include the price string as entered" in {
      val currency = Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))

      PurchaseDetails("1", currency).formatted mustBe "1, Euro (EUR)"
      PurchaseDetails("1.", currency).formatted mustBe "1., Euro (EUR)"
      PurchaseDetails("1.0", currency).formatted mustBe "1.0, Euro (EUR)"
      PurchaseDetails("1.00", currency).formatted mustBe "1.00, Euro (EUR)"
      PurchaseDetails("1.000", currency).formatted mustBe "1.000, Euro (EUR)"

      PurchaseDetails("01", currency).formatted mustBe "01, Euro (EUR)"
      PurchaseDetails("01.", currency).formatted mustBe "01., Euro (EUR)"
      PurchaseDetails("01.0", currency).formatted mustBe "01.0, Euro (EUR)"
      PurchaseDetails("01.00", currency).formatted mustBe "01.00, Euro (EUR)"
      PurchaseDetails("01.000", currency).formatted mustBe "01.000, Euro (EUR)"

      PurchaseDetails("0.1", currency).formatted mustBe "0.1, Euro (EUR)"
      PurchaseDetails("0.10", currency).formatted mustBe "0.10, Euro (EUR)"
      PurchaseDetails("0.100", currency).formatted mustBe "0.100, Euro (EUR)"

      PurchaseDetails(".01", currency).formatted mustBe ".01, Euro (EUR)"
      PurchaseDetails(".001", currency).formatted mustBe ".001, Euro (EUR)"
    }
  }

  "AmountInPence" should {
    "format correctly" in {
      AmountInPence(0).formattedInPounds mustBe "£0.00"
      AmountInPence(101).formattedInPounds mustBe "£1.01"
      AmountInPence(100101).formattedInPounds mustBe "£1,001.01"
    }
  }

  "GoodsEntry" should {
    "convert to a Goods" when {
      "the GoodsEntry is complete" in {
        completedImportGoods.goodsIfComplete mustBe Some(aImportGoods)
      }
    }

    "not convert to a Goods" when {
      "the GoodsEntry is incomplete" in {
        incompleteGoodsEntry.goodsIfComplete mustBe None
      }
    }
  }

  "GoodsEntries" should {
    "blow up if created with an empty sequence" in {
      intercept[RuntimeException] {
        GoodsEntries(Seq.empty)
      }.getMessage mustBe "GoodsEntries cannot be empty: use apply()"
    }

    "be complete" when {
      "all goods entries are complete" in {
        GoodsEntries(completedImportGoods).declarationGoodsIfComplete mustBe Some(DeclarationGoods(Seq(aImportGoods)))
      }
    }

    "be incomplete" when {
      "it is empty" in {
        GoodsEntries(Seq(ImportGoodsEntry())).declarationGoodsIfComplete mustBe None
      }

      "a goods entry is incomplete" in {
        incompleteGoodEntries.declarationGoodsIfComplete mustBe None
      }
    }
  }

  "JourneyOnFoot" should {
    "serialise and de-serialise" in {
      val journey = JourneyOnFoot(Port("LHR", "title.heathrow_airport", isGB = true, List("London Heathrow Airport", "LHR")), journeyDate)
      parse(toJson(journey).toString()).validate[JourneyOnFoot].get mustBe journey
    }
  }

  "JourneyInSmallVehicle" should {
    "serialise and de-serialise" in {
      val journey =
        JourneyInSmallVehicle(Port("DVR", "title.dover", isGB = true, List("Port of Dover")), journeyDate, vehicleRegistrationNumber)
      parse(toJson(journey).toString()).validate[JourneyInSmallVehicle].get mustBe journey
    }
  }

  "DeclarationJourney" should {
    "serialise and de-serialise" in {
      parse(toJson(completedDeclarationJourney).toString()).validate[DeclarationJourney].get mustBe completedDeclarationJourney
    }

    "be obfuscated" in {
      completedDeclarationJourney.obfuscated.maybeNameOfPersonCarryingTheGoods mustBe Some(Name("*****", "****"))
      completedDeclarationJourney.obfuscated.maybeEmailAddress mustBe Some(Email("***********"))
      completedDeclarationJourney.obfuscated.maybeCustomsAgentName mustBe Some("**********")
      completedDeclarationJourney.obfuscated.maybeCustomsAgentAddress mustBe
        Some(
          Address(
            lines = Seq("*************", "**********"),
            postcode = Some("*******"),
            country = AddressLookupCountry("**", Some("**************"))))
      completedDeclarationJourney.obfuscated.maybeEori mustBe Some(Eori("**************"))
      completedDeclarationJourney.obfuscated.maybeRegistrationNumber mustBe Some("******")
    }

    "have a customs agent" when {
      "the user has provided all the required details" in {
        completedDeclarationJourney.maybeCustomsAgent mustBe
          Some(
            CustomsAgent(completedDeclarationJourney.maybeCustomsAgentName.get, completedDeclarationJourney.maybeCustomsAgentAddress.get))
      }
    }

    "not have a customs agent" when {
      "the user has specified that they are not a customs agent" in {
        completedNonCustomsAgentJourney.maybeCustomsAgent mustBe None
      }

      "the user has not specified whether they are a customs agent" in {
        completedDeclarationJourney.copy(maybeIsACustomsAgent = None).maybeCustomsAgent mustBe None
      }

      "the user has not provided a customs agent name" in {
        completedDeclarationJourney.copy(maybeCustomsAgentName = None).maybeCustomsAgent mustBe None
      }

      "the user has not provided a customs agent address" in {
        completedDeclarationJourney.copy(maybeCustomsAgentAddress = None).maybeCustomsAgent mustBe None
      }
    }

    "be complete" when {
      "the user has completed the journey" in {
        val now = LocalDateTime.now
        val reference = MibReference("xx")
        val declarationId = DeclarationId(UUID.randomUUID().toString)
        completedDeclarationJourney.declarationIfRequiredAndComplete
          .map(_.copy(dateOfDeclaration = now).copy(mibReference = reference, declarationId = declarationId)) mustBe
          Some(
            Declaration(
              declarationId,
              aSessionId,
              DeclarationType.Import,
              GreatBritain,
              completedDeclarationJourney.goodsEntries.declarationGoodsIfComplete.get,
              completedDeclarationJourney.maybeNameOfPersonCarryingTheGoods.get,
              completedDeclarationJourney.maybeEmailAddress,
              completedDeclarationJourney.maybeCustomsAgent,
              completedDeclarationJourney.maybeEori.get,
              JourneyInSmallVehicle(
                Port("DVR", "title.dover", isGB = true, List("Port of Dover")),
                completedDeclarationJourney.maybeJourneyDetailsEntry.get.dateOfTravel,
                completedDeclarationJourney.maybeRegistrationNumber.get
              ),
              now,
              reference
            )
          )
      }

      "the user is not a customs agent" in {
        completedNonCustomsAgentJourney.declarationRequiredAndComplete mustBe true
      }

      "the user has supplied a customs agent name and address but then navigates back and answers 'No' to maybeIsACustomsAgent" in {
        completedDeclarationJourney.copy(maybeIsACustomsAgent = Some(No)).declarationIfRequiredAndComplete.isDefined mustBe true
      }

      "the user is not travelling by vehicle" in {
        completedDeclarationJourney
          .copy(
            maybeJourneyDetailsEntry = Some(heathrowJourneyEntry),
            maybeTravellingByVehicle = Some(No)
          )
          .declarationIfRequiredAndComplete
          .get
          .journeyDetails mustBe JourneyOnFoot(
          Port("LHR", "title.heathrow_airport", isGB = true, List("London Heathrow Airport", "LHR")),
          journeyDate)
      }

      "the trader is travelling by small vehicle has supplied the " + vehicleRegistrationNumber + "istration number of a small vehicle" in {
        completedDeclarationJourney
          .copy(
            maybeJourneyDetailsEntry = Some(doverJourneyEntry),
            maybeTravellingByVehicle = Some(Yes),
            maybeTravellingBySmallVehicle = Some(Yes),
            maybeRegistrationNumber = Some(vehicleRegistrationNumber)
          )
          .declarationIfRequiredAndComplete
          .get
          .journeyDetails mustBe JourneyInSmallVehicle(
          Port("DVR", "title.dover", isGB = true, List("Port of Dover")),
          journeyDate,
          vehicleRegistrationNumber)
      }
    }

    "be incomplete or not required" when {
      "journey details have not been completed" in {
        completedDeclarationJourney.copy(maybeJourneyDetailsEntry = None).declarationRequiredAndComplete mustBe false
      }

      "the place of arrival requires vehicle checks but the trader has not confirmed whether they are travelling by vehicle" in {
        completedDeclarationJourney
          .copy(
            maybeJourneyDetailsEntry = Some(doverJourneyEntry),
            maybeTravellingByVehicle = None
          )
          .declarationRequiredAndComplete mustBe false
      }

      "the place of arrival requires vehicle checks but the trader is travelling with a vehicle that is not small" in {
        completedDeclarationJourney
          .copy(
            maybeJourneyDetailsEntry = Some(doverJourneyEntry),
            maybeTravellingByVehicle = Some(Yes),
            maybeTravellingBySmallVehicle = Some(No)
          )
          .declarationRequiredAndComplete mustBe false
      }

      "the place of arrival requires vehicle checks and the trader is travelling with a vehicle but has not confirmed whether it is a small vehicle" in {
        completedDeclarationJourney
          .copy(
            maybeJourneyDetailsEntry = Some(doverJourneyEntry),
            maybeTravellingByVehicle = Some(Yes),
            maybeTravellingBySmallVehicle = None
          )
          .declarationRequiredAndComplete mustBe false
      }

      "the place of arrival requires vehicle checks and the trader has not supplied the " + vehicleRegistrationNumber + "istration number of their small vehicle" in {
        completedDeclarationJourney
          .copy(
            maybeJourneyDetailsEntry = Some(doverJourneyEntry),
            maybeTravellingByVehicle = Some(Yes),
            maybeTravellingBySmallVehicle = Some(Yes),
            maybeRegistrationNumber = None
          )
          .declarationRequiredAndComplete mustBe false
      }

      "the user has not confirmed whether they are carrying excise or restricted goods" in {
        completedDeclarationJourney.copy(maybeExciseOrRestrictedGoods = None).declarationRequiredAndComplete mustBe false
      }

      "the user has confirmed that they are carrying excise or restricted goods" in {
        completedDeclarationJourney.copy(maybeExciseOrRestrictedGoods = Some(Yes)).declarationRequiredAndComplete mustBe false
      }

      "the user has not confirmed the destination of the goods whether its GB or NI" in {
        completedDeclarationJourney.copy(maybeGoodsDestination = None).declarationRequiredAndComplete mustBe false
      }

      "the user has not confirmed whether the goods are below the threshold" in {
        completedDeclarationJourney.copy(maybeValueWeightOfGoodsBelowThreshold = None).declarationRequiredAndComplete mustBe false
      }

      "the user has confirmed that the goods are not below the threshold" in {
        completedDeclarationJourney.copy(maybeValueWeightOfGoodsBelowThreshold = Some(No)).declarationRequiredAndComplete mustBe false
      }

      "the user has not entered any goods" in {
        completedDeclarationJourney.copy(goodsEntries = GoodsEntries(Seq(ImportGoodsEntry()))).declarationRequiredAndComplete mustBe false
      }

      "the user has incomplete goods entries" in {
        completedDeclarationJourney.copy(goodsEntries = incompleteGoodEntries).declarationRequiredAndComplete mustBe false
      }

      "the user has not provided the name of the person carrying the goods" in {
        completedDeclarationJourney.copy(maybeNameOfPersonCarryingTheGoods = None).declarationRequiredAndComplete mustBe false
      }

      "the user has not provided an email address" in {
        completedDeclarationJourney.copy(maybeEmailAddress = None).declarationRequiredAndComplete mustBe false
      }

      "the user has not confirmed whether they are a customs agent" in {
        completedDeclarationJourney.copy(maybeIsACustomsAgent = None).declarationRequiredAndComplete mustBe false
      }

      "the user has confirmed they are a customs agent but does not provide the necessary details" in {
        completedDeclarationJourney.copy(maybeCustomsAgentName = None).declarationRequiredAndComplete mustBe false
        completedDeclarationJourney.copy(maybeCustomsAgentAddress = None).declarationRequiredAndComplete mustBe false
      }

      "the user has not provided an eori" in {
        completedDeclarationJourney.copy(maybeEori = None).declarationRequiredAndComplete mustBe false
      }

      "the user has not provided journey details" in {
        completedDeclarationJourney.copy(maybeJourneyDetailsEntry = None).declarationRequiredAndComplete mustBe false
      }
    }
  }

  "declaration" should {
    "serialise and de-serialise" in {
      val json = toJson(declaration)
      parse(json.toString()).validate[Declaration].get mustBe declaration
      (json \ "source").as[String] mustBe "Digital"
    }

    "provide current date and time formatted" in {
      val aDeclaration = declaration.copy(dateOfDeclaration = LocalDateTime.of(2020, 11, 10, 12, 55))

      aDeclaration.dateOfDeclaration.formattedDate mustBe "10 November 2020, 12:55pm"
    }
  }
}

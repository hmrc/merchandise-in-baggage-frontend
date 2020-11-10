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

import java.time.LocalDateTime

import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.MibReference
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.Declaration._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.Ports.{Dover, Heathrow}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.Currency
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpec, CoreTestData}

class DeclarationSpec extends BaseSpec with CoreTestData {
  private val completedNonCustomsAgentJourney = completedDeclarationJourney.copy(maybeIsACustomsAgent = Some(No))

  private val goods =
    Goods(
      completedGoodsEntry.maybeCategoryQuantityOfGoods.get,
      completedGoodsEntry.maybeGoodsVatRate.get,
      completedGoodsEntry.maybeCountryOfPurchase.get,
      completedGoodsEntry.maybePurchaseDetails.get,
      completedGoodsEntry.maybeInvoiceNumber.get)

  private val incompleteGoodsEntry = completedGoodsEntry.copy(maybeInvoiceNumber = None)
  private val incompleteGoodEntries = GoodsEntries(Seq(incompleteGoodsEntry))
  private val oneCompleteOneIncompleteGoodEntries = GoodsEntries(Seq(completedGoodsEntry, incompleteGoodsEntry))
  private val vehicleRegistrationNumber = "reg"

  "PurchaseDetails toString" should {
    "include the price string as entered" in {
      val currency = Currency("country", "currency", "ccy")

      PurchaseDetails("1", currency).toString mustBe "1, country currency (ccy)"
      PurchaseDetails("1.", currency).toString mustBe "1., country currency (ccy)"
      PurchaseDetails("1.0", currency).toString mustBe "1.0, country currency (ccy)"
      PurchaseDetails("1.00", currency).toString mustBe "1.00, country currency (ccy)"
      PurchaseDetails("1.000", currency).toString mustBe "1.000, country currency (ccy)"

      PurchaseDetails("01", currency).toString mustBe "01, country currency (ccy)"
      PurchaseDetails("01.", currency).toString mustBe "01., country currency (ccy)"
      PurchaseDetails("01.0", currency).toString mustBe "01.0, country currency (ccy)"
      PurchaseDetails("01.00", currency).toString mustBe "01.00, country currency (ccy)"
      PurchaseDetails("01.000", currency).toString mustBe "01.000, country currency (ccy)"

      PurchaseDetails("0.1", currency).toString mustBe "0.1, country currency (ccy)"
      PurchaseDetails("0.10", currency).toString mustBe "0.10, country currency (ccy)"
      PurchaseDetails("0.100", currency).toString mustBe "0.100, country currency (ccy)"

      PurchaseDetails(".01", currency).toString mustBe ".01, country currency (ccy)"
      PurchaseDetails(".001", currency).toString mustBe ".001, country currency (ccy)"
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
        completedGoodsEntry.goodsIfComplete mustBe Some(goods)
      }
    }

    "not convert to a Goods" when {
      "the GoodsEntry is incomplete" in {
        incompleteGoodsEntry.goodsIfComplete mustBe None
      }
    }
  }

  "GoodsEntries" should {
    "have an empty GoodsEntry by default" in {
      GoodsEntries().entries mustBe Seq(GoodsEntry.empty)
    }

    "blow up if created with an empty sequence" in {
      intercept[RuntimeException] {
        GoodsEntries(Seq.empty)
      }.getMessage mustBe "GoodsEntries cannot be empty: use apply()"
    }

    "be complete" when {
      "all goods entries are complete" in {
        GoodsEntries(completedGoodsEntry).declarationGoodsIfComplete mustBe Some(DeclarationGoods(goods))
      }
    }

    "be incomplete" when {
      "it is empty" in {
        GoodsEntries.empty.declarationGoodsIfComplete mustBe None
      }

      "a goods entry is incomplete" in {
        incompleteGoodEntries.declarationGoodsIfComplete mustBe None
      }
    }
  }

  "JourneyViaFootPassengerOnlyPort" should {
    "serialise and de-serialise" in {
      val journey = JourneyViaFootPassengerOnlyPort(Heathrow, journeyDate)
      parse(toJson(journey).toString()).validate[JourneyViaFootPassengerOnlyPort].get mustBe journey
    }
  }

  "JourneyOnFootViaVehiclePort" should {
    "serialise and de-serialise" in {
      val journey = JourneyOnFootViaVehiclePort(Dover, journeyDate)
      parse(toJson(journey).toString()).validate[JourneyOnFootViaVehiclePort].get mustBe journey
    }
  }

  "JourneyInSmallVehicle" should {
    "serialise and de-serialise" in {
      val journey = JourneyInSmallVehicle(Dover, journeyDate, vehicleRegistrationNumber)
      parse(toJson(journey).toString()).validate[JourneyInSmallVehicle].get mustBe journey
    }
  }

  "DeclarationJourney" should {
    "serialise and de-serialise" in {
      parse(toJson(completedDeclarationJourney).toString()).validate[DeclarationJourney].get mustBe completedDeclarationJourney
    }

    "have a customs agent" when {
      "the user has provided all the required details" in {
        completedDeclarationJourney.maybeCustomsAgent mustBe
          Some(
            CustomsAgent(
              completedDeclarationJourney.maybeCustomsAgentName.get,
              completedDeclarationJourney.maybeCustomsAgentAddress.get))
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
        completedDeclarationJourney.declarationIfRequiredAndComplete
          .map(_.copy(dateOfDeclaration = now).copy(mibReference = reference)) mustBe
          Some(Declaration(
            sessionId,
            DeclarationType.Import,
            GoodsDestinations.NorthernIreland,
            completedDeclarationJourney.goodsEntries.declarationGoodsIfComplete.get,
            completedDeclarationJourney.maybeNameOfPersonCarryingTheGoods.get,
            completedDeclarationJourney.maybeCustomsAgent,
            completedDeclarationJourney.maybeEori.get,
            JourneyInSmallVehicle(
              Dover,
              completedDeclarationJourney.maybeJourneyDetailsEntry.get.dateOfArrival,
              completedDeclarationJourney.maybeRegistrationNumber.get))
            .copy(dateOfDeclaration = now).copy(mibReference = reference))
      }

      "the user is not a customs agent" in {
        completedNonCustomsAgentJourney.declarationIfRequiredAndComplete.isDefined mustBe true
      }

      "the user has supplied a customs agent name and address but then navigates back and answers 'No' to maybeIsACustomsAgent" in {
        completedDeclarationJourney.copy(maybeIsACustomsAgent = Some(No)).declarationIfRequiredAndComplete.isDefined mustBe true
      }

      "the place of arrival does not require vehicle checks" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetailsEntry = Some(heathrowJourneyEntry)
        ).declarationIfRequiredAndComplete.get.journeyDetails mustBe JourneyViaFootPassengerOnlyPort(Heathrow, journeyDate)
      }

      "the place of arrival requires vehicle checks but the trader is not travelling by vehicle" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetailsEntry = Some(doverJourneyEntry), maybeTravellingByVehicle = Some(No)
        ).declarationIfRequiredAndComplete.get.journeyDetails mustBe JourneyOnFootViaVehiclePort(Dover, journeyDate)
      }

      "the place of arrival requires vehicle checks and the trader has supplied the " + vehicleRegistrationNumber + "istration number of a small vehicle" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetailsEntry = Some(doverJourneyEntry),
          maybeTravellingByVehicle = Some(Yes),
          maybeTravellingBySmallVehicle = Some(Yes),
          maybeRegistrationNumber = Some(vehicleRegistrationNumber)
        ).declarationIfRequiredAndComplete.get.journeyDetails mustBe JourneyInSmallVehicle(Dover, journeyDate, vehicleRegistrationNumber)
      }
    }

    "be incomplete or not required" when {
      "journey details have not been completed" in {
        completedDeclarationJourney.copy(maybeJourneyDetailsEntry = None).declarationIfRequiredAndComplete mustBe None
      }

      "the place of arrival requires vehicle checks but the trader has not confirmed whether they are travelling by vehicle" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetailsEntry = Some(doverJourneyEntry), maybeTravellingByVehicle = None
        ).declarationIfRequiredAndComplete mustBe None
      }

      "the place of arrival requires vehicle checks but the trader is travelling with a vehicle that is not small" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetailsEntry = Some(doverJourneyEntry),
          maybeTravellingByVehicle = Some(Yes),
          maybeTravellingBySmallVehicle = Some(No)
        ).declarationIfRequiredAndComplete mustBe None
      }

      "the place of arrival requires vehicle checks and the trader is travelling with a vehicle but has not confirmed whether it is a small vehicle" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetailsEntry = Some(doverJourneyEntry),
          maybeTravellingByVehicle = Some(Yes),
          maybeTravellingBySmallVehicle = None
        ).declarationIfRequiredAndComplete mustBe None
      }

      "the place of arrival requires vehicle checks and the trader has not supplied the " + vehicleRegistrationNumber + "istration number of their small vehicle" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetailsEntry = Some(doverJourneyEntry),
          maybeTravellingByVehicle = Some(Yes),
          maybeTravellingBySmallVehicle = Some(Yes),
          maybeRegistrationNumber = None
        ).declarationIfRequiredAndComplete mustBe None
      }
    }

    "be incomplete" when {
      "the user has not confirmed whether they are carrying excise or restricted goods" in {
        completedDeclarationJourney.copy(maybeExciseOrRestrictedGoods = None).declarationIfRequiredAndComplete mustBe None
      }

      "the user has confirmed that they are carrying excise or restricted goods" in {
        completedDeclarationJourney.copy(maybeExciseOrRestrictedGoods = Some(Yes)).declarationIfRequiredAndComplete mustBe None
      }

      "the user has not confirmed the destination of the goods" in {
        completedDeclarationJourney.copy(maybeGoodsDestination = None).declarationIfRequiredAndComplete mustBe None
      }

      "the user has not confirmed whether the goods exceed the threshold" in {
        completedDeclarationJourney.copy(maybeValueWeightOfGoodsExceedsThreshold = None).declarationIfRequiredAndComplete mustBe None
      }

      "the user has confirmed that the goods exceed the threshold" in {
        completedDeclarationJourney.copy(maybeValueWeightOfGoodsExceedsThreshold = Some(Yes)).declarationIfRequiredAndComplete mustBe None
      }

      "the user has not entered any goods" in {
        completedDeclarationJourney.copy(goodsEntries = GoodsEntries.empty).declarationIfRequiredAndComplete mustBe None
      }

      "the user has incomplete goods entries" in {
        completedDeclarationJourney.copy(goodsEntries = incompleteGoodEntries).declarationIfRequiredAndComplete mustBe None
      }

      "the user has not provided the name of the person carrying the goods" in {
        completedDeclarationJourney.copy(maybeNameOfPersonCarryingTheGoods = None).declarationIfRequiredAndComplete mustBe None
      }

      "the user has not confirmed whether they are a customs agent" in {
        completedDeclarationJourney.copy(maybeIsACustomsAgent = None).declarationIfRequiredAndComplete mustBe None
      }

      "the user has confirmed they are a customs agent but does not provide the necessary details" in {
        completedDeclarationJourney.copy(maybeCustomsAgentName = None).declarationIfRequiredAndComplete mustBe None
        completedDeclarationJourney.copy(maybeCustomsAgentAddress = None).declarationIfRequiredAndComplete mustBe None
      }

      "the user has not provided an eori" in {
        completedDeclarationJourney.copy(maybeEori = None).declarationIfRequiredAndComplete mustBe None
      }

      "the user has not provided journey details" in {
        completedDeclarationJourney.copy(maybeJourneyDetailsEntry = None).declarationIfRequiredAndComplete mustBe None
      }
    }
  }

  "declaration" should {
    "serialise and de-serialise" in {
      parse(toJson(declaration).toString()).validate[Declaration].get mustBe declaration
    }

    "provide current date and time formatted" in {
      val aDeclaration = declaration.copy(dateOfDeclaration = LocalDateTime.of(2020, 11, 10, 12, 55))

      aDeclaration.dateOfDeclaration.formattedDate mustBe "10 November 2020, 12:55 PM"
    }
  }
}

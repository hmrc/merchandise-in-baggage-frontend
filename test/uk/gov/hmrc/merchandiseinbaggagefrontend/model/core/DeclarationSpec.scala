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

import java.time.LocalDate.now

import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.PlacesOfArrival.{Dover, Heathrow}
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpec, CoreTestData}

class DeclarationSpec extends BaseSpec with CoreTestData {
  private val completedNonCustomsAgentJourney = completedDeclarationJourney.copy(maybeIsACustomsAgent = Some(false))

  private val goods =
    Goods(
      completedGoodsEntry.categoryQuantityOfGoods,
      completedGoodsEntry.maybeGoodsVatRate.get,
      completedGoodsEntry.maybeCountryOfPurchase.get,
      completedGoodsEntry.maybePurchaseDetails.get,
      completedGoodsEntry.maybeInvoiceNumber.get,
      completedGoodsEntry.maybeTaxDue.get)

  private val incompleteGoodsEntry = completedGoodsEntry.copy(maybeTaxDue = None)
  private val incompleteGoodEntries = GoodsEntries(Seq(completedGoodsEntry, incompleteGoodsEntry))

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

  "DeclarationJourney" should {
    "serialise and de-serialise" in {
      parse(toJson(completedDeclarationJourney).toString()).validate[DeclarationJourney].asOpt mustBe Some(completedDeclarationJourney)
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

    "be complete and required" when {
      "the place of arrival does not require vehicle checks" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetails = Some(JourneyDetails(Heathrow, now))
        ).journeyDetailsCompleteAndDeclarationRequired mustBe true
      }

      "the place of arrival requires vehicle checks but the trader is not travelling by vehicle" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetails = Some(JourneyDetails(Dover, now)), maybeTravellingByVehicle = Some(false)
        ).journeyDetailsCompleteAndDeclarationRequired mustBe true
      }

      "the place of arrival requires vehicle checks and the trader has supplied the registration number of a small vehicle" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetails = Some(JourneyDetails(Dover, now)),
          maybeTravellingByVehicle = Some(true),
          maybeTravellingBySmallVehicle = Some(true),
          maybeRegistrationNumber = Some("reg")
        ).journeyDetailsCompleteAndDeclarationRequired mustBe true
      }
    }

    "be incomplete or not required" when {
      "journey details have not been completed" in {
        completedDeclarationJourney.copy(maybeJourneyDetails = None).journeyDetailsCompleteAndDeclarationRequired mustBe false
      }

      "the place of arrival requires vehicle checks but the trader has not confirmed whether they are travelling by vehicle" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetails = Some(JourneyDetails(Dover, now)), maybeTravellingByVehicle = None
        ).journeyDetailsCompleteAndDeclarationRequired mustBe false
      }

      "the place of arrival requires vehicle checks but the trader is travelling with a vehicle that is not small" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetails = Some(JourneyDetails(Dover, now)),
          maybeTravellingByVehicle = Some(true),
          maybeTravellingBySmallVehicle = Some(false)
        ).journeyDetailsCompleteAndDeclarationRequired mustBe false
      }

      "the place of arrival requires vehicle checks and the trader is travelling with a vehicle but has not confirmed whether it is a small vehicle" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetails = Some(JourneyDetails(Dover, now)),
          maybeTravellingByVehicle = Some(true),
          maybeTravellingBySmallVehicle = None
        ).journeyDetailsCompleteAndDeclarationRequired mustBe false
      }

      "the place of arrival requires vehicle checks and the trader has not supplied the registration number of their small vehicle" in {
        completedDeclarationJourney.copy(
          maybeJourneyDetails = Some(JourneyDetails(Dover, now)),
          maybeTravellingByVehicle = Some(true),
          maybeTravellingBySmallVehicle = Some(true),
          maybeRegistrationNumber = None
        ).journeyDetailsCompleteAndDeclarationRequired mustBe false
      }
    }

    "be complete" when {
      "the user has completed the journey" in {
        completedDeclarationJourney.declarationIfRequiredAndComplete mustBe
          Some(Declaration(
            sessionId,
            completedDeclarationJourney.goodsEntries.declarationGoodsIfComplete.get,
            completedDeclarationJourney.maybeNameOfPersonCarryingTheGoods.get,
            completedDeclarationJourney.maybeCustomsAgent,
            completedDeclarationJourney.maybeEori.get,
            completedDeclarationJourney.maybeJourneyDetails.get,
            completedDeclarationJourney.maybeTravellingByVehicle.get,
            completedDeclarationJourney.maybeRegistrationNumber))
      }
    }

    "be incomplete" when {
      "the user has not confirmed whether they are carrying excise or restricted goods" in {
        completedDeclarationJourney.copy(maybeExciseOrRestrictedGoods = None).declarationIfRequiredAndComplete mustBe None
      }

      "the user has confirmed that they are carrying excise or restricted goods" in {
        completedDeclarationJourney.copy(maybeExciseOrRestrictedGoods = Some(true)).declarationIfRequiredAndComplete mustBe None
      }

      "the user has not confirmed the destination of the goods" in {
        completedDeclarationJourney.copy(maybeGoodsDestination = None).declarationIfRequiredAndComplete mustBe None
      }

      "the user has not confirmed whether the goods exceed the threshold" in {
        completedDeclarationJourney.copy(maybeValueWeightOfGoodsExceedsThreshold = None).declarationIfRequiredAndComplete mustBe None
      }

      "the user has confirmed that the goods exceed the threshold" in {
        completedDeclarationJourney.copy(maybeValueWeightOfGoodsExceedsThreshold = Some(true)).declarationIfRequiredAndComplete mustBe None
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
        completedDeclarationJourney.copy(maybeJourneyDetails = None).declarationIfRequiredAndComplete mustBe None
      }
    }
  }

  "declaration" should {
    "serialise and de-serialise" in {
      parse(toJson(declaration).toString()).validate[Declaration].asOpt mustBe Some(declaration)
    }
  }
}

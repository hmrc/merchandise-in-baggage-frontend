/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import com.softwaremill.quicklens._
import org.scalamock.scalatest.MockFactory
import play.api.mvc.Call
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{OverThreshold, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, JourneyType, Paid}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries, PurchaseDetailsInput}
import uk.gov.hmrc.merchandiseinbaggage.navigation._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NavigatorSpec extends DeclarationJourneyControllerSpec with PropertyBaseTables with MockFactory {

  forAll(declarationTypesTable) { importOrExport: DeclarationType =>
    forAll(journeyTypesTable) { newOrAmend: JourneyType =>
      s"On ${ExciseAndRestrictedGoodsController.onPageLoad.url}" must {
        s"redirect to ${CannotUseServiceController.onPageLoad.url} if submit with Yes for $importOrExport and $newOrAmend" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(ExciseAndRestrictedGoodsRequest(Yes, journey, _ => Future(journey), false))

          result.futureValue mustBe CannotUseServiceController.onPageLoad
        }

        if (newOrAmend == Amend) {
          s"redirect to ${ValueWeightOfGoodsController.onPageLoad.url} for $newOrAmend on submit for $importOrExport" in new Navigator {
            val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
            val result = nextPage(ExciseAndRestrictedGoodsRequest(No, journey, _ => Future(journey), false))

            result.futureValue mustBe ValueWeightOfGoodsController.onPageLoad
          }
        }

        if (newOrAmend == New) {
          s"redirect to ${ValueWeightOfGoodsController.onPageLoad.url} for $newOrAmend on submit for $importOrExport" in new Navigator {
            val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
            val result = nextPage(ExciseAndRestrictedGoodsRequest(No, journey, _ => Future(journey), false))

            result.futureValue mustBe ValueWeightOfGoodsController.onPageLoad
          }
        }
      }
      s"from ${EnterEmailController.onPageLoad.url} navigates to ${JourneyDetailsController.onPageLoad.url} for $newOrAmend & $importOrExport" in new Navigator {
        val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
        val result: Future[Call] = nextPage(EnterEmailRequest(journey, _ => Future(journey), false))

        result.futureValue mustBe JourneyDetailsController.onPageLoad
      }

      s"on ${EoriNumberController.onPageLoad}" should {
        s"navigates to ${TravellerDetailsController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result: Future[Call] = nextPage(EoriNumberRequest(journey, _ => Future(journey), false))

          result.futureValue mustBe TravellerDetailsController.onPageLoad
        }
      }

      s"from ${TravellerDetailsController.onPageLoad.url} navigates to ${EnterEmailController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
        val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
        val result = nextPage(TravellerDetailsRequest(journey, _ => Future(journey), false))

        result.futureValue mustBe EnterEmailController.onPageLoad
      }

      s"on ${ValueWeightOfGoodsController.onPageLoad.url} submit" must {
        s"navigates to ${CannotUseServiceController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(ValueWeightOfGoodsRequest(No, 1, journey, _ => Future(journey), false))

          result.futureValue mustBe CannotUseServiceController.onPageLoad
        }

        s"navigates to ${GoodsTypeController.onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(ValueWeightOfGoodsRequest(Yes, 1, journey, _ => Future(journey), false))

          result.futureValue mustBe GoodsTypeController.onPageLoad(1)
        }
      }

      s"on ${GoodsDestinationController.onPageLoad.url} submit" must {
        s"navigates to ${ExciseAndRestrictedGoodsController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result: Future[Call] = nextPage(GoodsDestinationRequest(GreatBritain, journey, _ => Future(journey), false))

          result.futureValue mustBe ExciseAndRestrictedGoodsController.onPageLoad
        }

        s"navigates to ${CannotUseServiceIrelandController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result: Future[Call] = nextPage(GoodsDestinationRequest(NorthernIreland, journey, _ => Future(journey), false))

          result.futureValue mustBe CannotUseServiceIrelandController.onPageLoad
        }
      }

      s"from ${VehicleRegistrationNumberController.onPageLoad.url} navigates to ${CheckYourAnswersController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Future[Call] = nextPage(
          VehicleRegistrationNumberRequest(completedDeclarationJourney, "LX123", _ => Future.successful(completedDeclarationJourney)))

        result.futureValue mustBe CheckYourAnswersController.onPageLoad
      }

      s"on ${AgentDetailsController.onPageLoad.url} submit" must {
        s"navigates to ${EnterAgentAddressController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
          val journey = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(AgentDetailsRequest("agent", journey, _ => Future(journey)))

          result.futureValue mustBe EnterAgentAddressController.onPageLoad
        }
      }

      s"on ${VehicleSizeController.onPageLoad.url} submit" must {
        s"navigates to ${VehicleRegistrationNumberController.onPageLoad} for $newOrAmend & $importOrExport if Yes" in new Navigator {
          val journey = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(VehicleSizeRequest(Yes, journey, _ => Future(journey), false))

          result.futureValue mustBe VehicleRegistrationNumberController.onPageLoad
        }

        s"navigates to ${CannotUseServiceController.onPageLoad} for $newOrAmend & $importOrExport if No" in new Navigator {
          val journey = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(VehicleSizeRequest(No, journey, _ => Future(journey), false))

          result.futureValue mustBe CannotUseServiceController.onPageLoad
        }
      }

      s"on ${GoodsInVehicleController.onPageLoad.url} submit" must {
        s"navigates to ${VehicleSizeController.onPageLoad} if Yes for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(GoodsInVehicleRequest(Yes, journey, _ => Future(journey), false))

          result.futureValue mustBe VehicleSizeController.onPageLoad
        }

        s"navigates to ${CheckYourAnswersController.onPageLoad} if No for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(GoodsInVehicleRequest(No, journey, _ => Future(journey), false))

          result.futureValue mustBe CheckYourAnswersController.onPageLoad
        }
      }

      s"from ${GoodsOriginController.onPageLoad(1).url} navigates to ${PurchaseDetailsController
        .onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
        val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
        val entries = if (importOrExport == Import) startedImportGoods else startedExportGoods
        val result: Future[Call] = nextPage(GoodsOriginRequest(journey, entries, 1, _ => Future(journey)))

        result.futureValue mustBe GoodsVatRateController.onPageLoad(1)
      }

      if (importOrExport == Import) {
        s"from ${GoodsTypeController.onPageLoad(1).url} navigates to ${GoodsVatRateController
          .onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result: Future[Call] =
            nextPage(GoodsTypeRequest(journey, startedImportGoods, 1, aCategoryOfGoods, _ => Future(journey)))

          result.futureValue mustBe PurchaseDetailsController.onPageLoad(1)
        }
      }

      if (importOrExport == Export) {
        s"from ${GoodsTypeController.onPageLoad(1).url} navigates to ${SearchGoodsCountryController
          .onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result: Future[Call] =
            nextPage(GoodsTypeRequest(journey, startedExportGoods, 1, aCategoryOfGoods, _ => Future(journey)))

          result.futureValue mustBe PurchaseDetailsController.onPageLoad(1)
        }
      }

      s"from ${GoodsVatRateController.onPageLoad(1).url} navigates to ${ReviewGoodsController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
        val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
        val result: Future[Call] = nextPage(GoodsVatRateRequest(journey, startedImportGoods, 1, _ => Future(journey)))

        result.futureValue mustBe ReviewGoodsController.onPageLoad
      }

      s"from ${SearchGoodsCountryController.onPageLoad(2).url} navigates to ${ReviewGoodsController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
        val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
        val entries = if (importOrExport == Import) startedImportGoods else startedExportGoods
        val result: Future[Call] = nextPage(SearchGoodsCountryRequest(journey, entries, 2, _ => Future(journey)))

        result.futureValue mustBe ReviewGoodsController.onPageLoad
      }

      s"from ${JourneyDetailsController.onPageLoad.url} navigates to ${GoodsInVehicleController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
        val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
        val result = nextPage(JourneyDetailsRequest(journey, _ => Future(journey), false))

        result.futureValue mustBe GoodsInVehicleController.onPageLoad
      }

      s"from ${PreviousDeclarationDetailsController.onPageLoad.url} navigates to ${ExciseAndRestrictedGoodsController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
        val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
        val result = nextPage(PreviousDeclarationDetailsRequest(journey, journey.toDeclaration, _ => Future(journey)))

        result.futureValue mustBe ExciseAndRestrictedGoodsController.onPageLoad
      }

      s"from ${PurchaseDetailsController.onPageLoad(1).url} navigates to ${GoodsOriginController
        .onPageLoad(1)} for $newOrAmend & $importOrExport updating goods entries" in new Navigator {
        val detailsInput: PurchaseDetailsInput = PurchaseDetailsInput("123", "EUR")
        val stubUpsert: DeclarationJourney => Future[DeclarationJourney] =
          _ => Future.successful(completedDeclarationJourney) //TODO make it work with mockFunction

        val result = nextPage(PurchaseDetailsRequest(detailsInput, 1, startedImportGoods, importJourneyWithStartedGoodsEntry, stubUpsert))

        result.futureValue mustBe GoodsOriginController.onPageLoad(1)
      }

      if (newOrAmend == New) {
        s"from ${NewOrExistingController.onPageLoad.url} navigates to ${GoodsDestinationController.onPageLoad} for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(NewOrExistingRequest(journey, _ => Future(journey), false))

          result.futureValue mustBe GoodsDestinationController.onPageLoad
        }
      }

      s"from ${ReviewGoodsController.onPageLoad.url} navigates to /declare-commercial-goods/goods-type/idx + 1 " +
        s"for $newOrAmend & $importOrExport" in new Navigator {
        val updatedJourney: DeclarationJourney = completedDeclarationJourney
          .updateGoodsEntries()
          .copy(declarationType = importOrExport, journeyType = newOrAmend)
        val expectedUpdatedEntries: Int = completedDeclarationJourney.goodsEntries.entries.size + 1

        val result: Future[Call] = nextPage(
          ReviewGoodsRequest(
            Yes,
            completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend),
            WithinThreshold,
            _ => Future.successful(updatedJourney)
          ))

        result.futureValue mustBe GoodsTypeController.onPageLoad(expectedUpdatedEntries)
      }

      s"from ${ReviewGoodsController.onPageLoad.url} navigates to ${PaymentCalculationController.onPageLoad} if answer No without updating goods entries for $newOrAmend & $importOrExport" in new Navigator {
        val result: Future[Call] = nextPage(
          ReviewGoodsRequest(
            No,
            incompleteDeclarationJourney.copy(declarationType = importOrExport),
            WithinThreshold,
            _ => Future.successful(incompleteDeclarationJourney.copy(declarationType = importOrExport))
          ))

        result.futureValue mustBe PaymentCalculationController.onPageLoad
      }

      s"from ${ReviewGoodsController.onPageLoad.url} navigates to ${GoodsOverThresholdController.onPageLoad} if answer No $newOrAmend & $importOrExport and over threshold" in new Navigator {
        val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
        val result: Future[Call] = nextPage(
          ReviewGoodsRequest(
            No,
            journey,
            OverThreshold,
            _ => Future.successful(journey)
          ))

        result.futureValue mustBe GoodsOverThresholdController.onPageLoad
      }

      s"from ${ReviewGoodsController.onPageLoad.url} navigates to ${GoodsOverThresholdController.onPageLoad} if over for $newOrAmend & $importOrExport" in new Navigator {
        val journey: DeclarationJourney =
          importJourneyWithGoodsOverThreshold.copy(declarationType = importOrExport, journeyType = newOrAmend)
        val result: Future[Call] = nextPage(
          ReviewGoodsRequest(
            Yes,
            journey,
            OverThreshold,
            _ => Future.successful(journey)
          ))

        result.futureValue mustBe GoodsOverThresholdController.onPageLoad
      }

      "on RemoveGoodsControllerRequest submit" should {
        s"from ${RemoveGoodsController.onPageLoad(1).url} navigates to ${GoodsRemovedController.onPageLoad} " +
          s"if over for $newOrAmend & $importOrExport Yes and entries are 1" in new Navigator {
          val oneSizeEntries: GoodsEntries = GoodsEntries(if (importOrExport == Import) startedImportGoods else startedExportGoods)
          val journey: DeclarationJourney =
            completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend, goodsEntries = oneSizeEntries)
          val result: Future[Call] = nextPage(
            RemoveGoodsRequest(
              1,
              journey,
              Yes,
              _ => Future.successful(journey)
            ))

          oneSizeEntries.entries.size mustBe 1
          result.futureValue mustBe GoodsRemovedController.onPageLoad
        }

        s"from ${RemoveGoodsController.onPageLoad(1).url} navigates to ${CheckYourAnswersController.onPageLoad} " +
          s"if over for $newOrAmend & $importOrExport Yes and entries > 1" in new Navigator {
          val twoSizeEntries: GoodsEntries =
            GoodsEntries(if (importOrExport == Import) completedImportGoods else completedExportGoods)
              .modify(_.entries)
              .using(e => e.+:(e.head))

          val journey: DeclarationJourney =
            completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend, goodsEntries = twoSizeEntries)

          val result: Future[Call] = nextPage(
            RemoveGoodsRequest(
              1,
              journey,
              Yes,
              _ => Future.successful(journey)
            ))

          twoSizeEntries.entries.size mustBe 2
          newOrAmend match {
            case New =>
              journey.declarationRequiredAndComplete mustBe true
              result.futureValue mustBe CheckYourAnswersController.onPageLoad
            case Amend => result.futureValue mustBe ReviewGoodsController.onPageLoad
          }
        }
        s"from ${RemoveGoodsController.onPageLoad(1).url} navigates to ${CheckYourAnswersController.onPageLoad} " +
          s"if over for $newOrAmend & $importOrExport No and completed" in new Navigator {
          val twoSizeEntries: GoodsEntries =
            GoodsEntries(if (importOrExport == Import) completedImportGoods else completedExportGoods)
              .modify(_.entries)
              .using(e => e.+:(e.head))

          val journey: DeclarationJourney =
            completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend, goodsEntries = twoSizeEntries)

          val result: Future[Call] = nextPage(
            RemoveGoodsRequest(
              1,
              journey,
              No,
              _ => Future.successful(journey)
            ))

          twoSizeEntries.entries.size mustBe 2
          newOrAmend match {
            case New =>
              journey.declarationRequiredAndComplete mustBe true
              result.futureValue mustBe CheckYourAnswersController.onPageLoad
            case Amend => result.futureValue mustBe ReviewGoodsController.onPageLoad
          }
        }
      }

      s"on $RetrieveDeclarationController submit" should {
        s"navigate to $PreviousDeclarationDetailsController and update if found declaration for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(
            RetrieveDeclarationRequest(
              Some(journey.toDeclaration.modify(_.paymentStatus).setTo(Some(Paid))),
              journey,
              _ => Future.successful(journey)
            ))

          result.futureValue mustBe PreviousDeclarationDetailsController.onPageLoad
        }

        s"navigate to $DeclarationNotFoundController if NOT found declaration for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(
            RetrieveDeclarationRequest(
              None,
              journey,
              _ => Future.successful(journey)
            ))
          result.futureValue mustBe DeclarationNotFoundController.onPageLoad
        }

        s"navigate to $DeclarationNotFoundController if payment status is invalid for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPage(
            RetrieveDeclarationRequest(
              Some(journey.toDeclaration.modify(_.paymentStatus).setTo(None)),
              journey,
              _ => Future.successful(journey)
            ))
          if (importOrExport == Import) result.futureValue mustBe DeclarationNotFoundController.onPageLoad
        }
      }

      s"on $CustomsAgentController submit" should {
        s"navigate to ${AgentDetailsController.onPageLoad} if Yes for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney
            .copy(declarationType = importOrExport, journeyType = newOrAmend)
          val eventualCall: Future[Call] = nextPage(CustomsAgentRequest(Yes, journey, _ => Future.successful(journey), false))

          eventualCall.futureValue mustBe AgentDetailsController.onPageLoad
        }

        s"navigate to ${EoriNumberController.onPageLoad} if No for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val eventualCall: Future[Call] = nextPage(CustomsAgentRequest(No, journey, _ => Future.successful(journey), false))

          eventualCall.futureValue mustBe EoriNumberController.onPageLoad
        }
      }
    }
  }

  s"from ${ReviewGoodsController.onPageLoad.url} navigates to ${GoodsOverThresholdController.onPageLoad} if answer No & Export and over threshold" in new Navigator {
    val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = Export, journeyType = Amend)
    val result: Future[Call] = nextPage(
      ReviewGoodsRequest(
        No,
        journey,
        OverThreshold,
        _ => Future.successful(journey)
      ))

    result.futureValue mustBe GoodsOverThresholdController.onPageLoad
  }
}

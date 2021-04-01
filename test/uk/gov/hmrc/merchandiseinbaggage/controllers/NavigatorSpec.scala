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
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, JourneyType, Paid, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries, PurchaseDetailsInput}
import uk.gov.hmrc.merchandiseinbaggage.navigation._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NavigatorSpec extends DeclarationJourneyControllerSpec with PropertyBaseTables with MockFactory {

  forAll(declarationTypesTable) { importOrExport: DeclarationType =>
    forAll(journeyTypesTable) { newOrAmend: JourneyType =>
      s"On ${ExciseAndRestrictedGoodsController.onPageLoad().url}" must {
        s"redirect to ${CannotUseServiceController.onPageLoad().url} if submit with Yes for $importOrExport and $newOrAmend" in new Navigator {
          val result: Call = nextPage(RequestWithIndex(ExciseAndRestrictedGoodsController.onPageLoad().url, Yes, newOrAmend, 1))

          result mustBe CannotUseServiceController.onPageLoad()
        }

        if (newOrAmend == Amend) {
          s"redirect to ${ValueWeightOfGoodsController.onPageLoad().url} for $newOrAmend on submit for $importOrExport" in new Navigator {
            val result: Call = nextPage(RequestWithIndex(ExciseAndRestrictedGoodsController.onPageLoad().url, No, newOrAmend, 1))

            result mustBe ValueWeightOfGoodsController.onPageLoad()
          }
        }

        if (newOrAmend == New) {
          s"redirect to ${ValueWeightOfGoodsController.onPageLoad().url} for $newOrAmend on submit for $importOrExport" in new Navigator {
            val result: Call = nextPage(RequestWithIndex(ExciseAndRestrictedGoodsController.onPageLoad().url, No, newOrAmend, 1))

            result mustBe ValueWeightOfGoodsController.onPageLoad()
          }
        }
      }
      s"from ${EnterEmailController.onPageLoad().url} navigates to ${JourneyDetailsController.onPageLoad().url} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPass(EnterEmailController.onPageLoad().url))

        result mustBe JourneyDetailsController.onPageLoad()
      }

      s"from ${EoriNumberController.onPageLoad().url} navigates to ${TravellerDetailsController.onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPass(EoriNumberController.onPageLoad().url))

        result mustBe TravellerDetailsController.onPageLoad()
      }

      s"from ${TravellerDetailsController.onPageLoad().url} navigates to ${EnterEmailController.onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPass(TravellerDetailsController.onPageLoad().url))

        result mustBe EnterEmailController.onPageLoad()
      }

      s"from ${ValueWeightOfGoodsController.onPageLoad().url} navigates to ${CannotUseServiceController.onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPassWithIndexAndValue(No, 1))

        result mustBe CannotUseServiceController.onPageLoad()
      }

      s"from ${ValueWeightOfGoodsController.onPageLoad().url} navigates to ${GoodsTypeQuantityController.onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPassWithIndexAndValue(Yes, 1))

        result mustBe GoodsTypeQuantityController.onPageLoad(1)
      }

      s"from ${GoodsDestinationController.onPageLoad().url} navigates to ${ExciseAndRestrictedGoodsController
        .onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestWithAnswer(GoodsDestinationController.onPageLoad().url, GreatBritain))

        result mustBe ExciseAndRestrictedGoodsController.onPageLoad()
      }

      s"from ${GoodsDestinationController.onPageLoad().url} navigates to ${CannotUseServiceIrelandController
        .onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestWithAnswer(GoodsDestinationController.onPageLoad().url, NorthernIreland))

        result mustBe CannotUseServiceIrelandController.onPageLoad()
      }

      s"from ${VehicleRegistrationNumberController.onPageLoad().url} navigates to ${CheckYourAnswersController
        .onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Future[Call] = nextPageWithCallBack(
          VehicleRegistrationNumberControllerRequest(
            completedDeclarationJourney,
            "LX123",
            _ => Future.successful(completedDeclarationJourney)))

        result.futureValue mustBe CheckYourAnswersController.onPageLoad()
      }

      s"from ${VehicleSizeController.onPageLoad().url} navigates to ${VehicleRegistrationNumberController
        .onPageLoad()} for $newOrAmend & $importOrExport if Yes" in new Navigator {
        val result: Call = nextPage(RequestWithAnswer[YesNo](VehicleSizeController.onPageLoad().url, Yes))

        result mustBe VehicleRegistrationNumberController.onPageLoad()
      }

      s"from ${VehicleSizeController.onPageLoad().url} navigates to ${CannotUseServiceController
        .onPageLoad()} for $newOrAmend & $importOrExport if No" in new Navigator {
        val result: Call = nextPage(RequestWithAnswer[YesNo](VehicleSizeController.onPageLoad().url, No))

        result mustBe CannotUseServiceController.onPageLoad()
      }

      s"from ${GoodsInVehicleController.onPageLoad().url} navigates to ${VehicleSizeController
        .onPageLoad()} if Yes for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestWithAnswer(GoodsInVehicleController.onPageLoad().url, Yes))

        result mustBe VehicleSizeController.onPageLoad()
      }

      s"from ${GoodsInVehicleController.onPageLoad().url} navigates to ${CheckYourAnswersController
        .onPageLoad()} if No for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestWithAnswer(GoodsInVehicleController.onPageLoad().url, No))

        result mustBe CheckYourAnswersController.onPageLoad()
      }

      s"from ${GoodsOriginController.onPageLoad(1).url} navigates to ${PurchaseDetailsController
        .onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPassWithIndex(GoodsOriginController.onPageLoad(1).url, 1))

        result mustBe PurchaseDetailsController.onPageLoad(1)
      }

      if (importOrExport == Import) {
        s"from ${GoodsTypeQuantityController.onPageLoad(1).url} navigates to ${GoodsVatRateController
          .onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
          val result: Call = nextPage(RequestWithDeclarationType(GoodsTypeQuantityController.onPageLoad(1).url, importOrExport, 1))

          result mustBe GoodsVatRateController.onPageLoad(1)
        }
      }

      if (importOrExport == Export) {
        s"from ${GoodsTypeQuantityController.onPageLoad(1).url} navigates to ${SearchGoodsCountryController
          .onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
          val result: Call = nextPage(RequestWithDeclarationType(GoodsTypeQuantityController.onPageLoad(1).url, importOrExport, 1))

          result mustBe SearchGoodsCountryController.onPageLoad(1)
        }
      }
      s"from ${GoodsVatRateController.onPageLoad(1).url} navigates to ${SearchGoodsCountryController
        .onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPassWithIndex(GoodsVatRateController.onPageLoad(1).url, 1))

        result mustBe SearchGoodsCountryController.onPageLoad(1)
      }

      s"from ${SearchGoodsCountryController.onPageLoad(2).url} navigates to ${PurchaseDetailsController
        .onPageLoad(2)} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPassWithIndex(SearchGoodsCountryController.onPageLoad(2).url, 2))

        result mustBe PurchaseDetailsController.onPageLoad(2)
      }

      s"from ${JourneyDetailsController.onPageLoad().url} navigates to ${GoodsInVehicleController
        .onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPass(JourneyDetailsController.onPageLoad().url))

        result mustBe GoodsInVehicleController.onPageLoad()
      }

      s"from ${PreviousDeclarationDetailsController.onPageLoad().url} navigates to ${ExciseAndRestrictedGoodsController
        .onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPass(PreviousDeclarationDetailsController.onPageLoad().url))

        result mustBe ExciseAndRestrictedGoodsController.onPageLoad()
      }

      s"from ${PurchaseDetailsController.onPageLoad(1).url} navigates to ${ReviewGoodsController
        .onPageLoad()} for $newOrAmend & $importOrExport updating goods entries" in new Navigator {
        val detailsInput: PurchaseDetailsInput = PurchaseDetailsInput("123", "EUR")
        val stubUpsert: DeclarationJourney => Future[DeclarationJourney] =
          _ => Future.successful(completedDeclarationJourney) //TODO make it work with mockFunction

        val result = nextPageWithCallBack(
          RequestWithIndexAndCallBack(
            detailsInput,
            1,
            completedGoodsEntries(importOrExport).entries.head,
            completedDeclarationJourney,
            stubUpsert))

        result.futureValue mustBe ReviewGoodsController.onPageLoad()
      }

      if (newOrAmend == New) {
        s"from ${NewOrExistingController.onPageLoad().url} navigates to ${GoodsDestinationController
          .onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
          val result: Call = nextPage(RequestWithAnswer(NewOrExistingController.onPageLoad().url, newOrAmend))

          result mustBe GoodsDestinationController.onPageLoad()
        }
      }

      s"from ${ReviewGoodsController.onPageLoad().url} navigates to /declare-commercial-goods/goods-type-quantity/idx + 1 " +
        s"for $newOrAmend & $importOrExport" in new Navigator {
        val updatedJourney: DeclarationJourney = completedDeclarationJourney
          .updateGoodsEntries()
          .copy(declarationType = importOrExport, journeyType = newOrAmend)
        val expectedUpdatedEntries: Int = completedDeclarationJourney.goodsEntries.entries.size + 1

        val result: Future[Call] = nextPageWithCallBack(
          RequestWithCallBack(
            ReviewGoodsController.onPageLoad().url,
            Yes,
            GoodsEntries(if (importOrExport == Import) startedImportGoods else startedExportGoods),
            completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend),
            false,
            _ => Future.successful(updatedJourney)
          ))

        result.futureValue mustBe GoodsTypeQuantityController.onPageLoad(expectedUpdatedEntries)
      }

      s"from ${ReviewGoodsController.onPageLoad().url} navigates to ${PaymentCalculationController
        .onPageLoad()} if answer No without updating goods entries for $newOrAmend & $importOrExport" in new Navigator {
        val result: Future[Call] = nextPageWithCallBack(
          RequestWithCallBack(
            ReviewGoodsController.onPageLoad().url,
            No,
            GoodsEntries(if (importOrExport == Import) startedImportGoods else startedExportGoods),
            incompleteDeclarationJourney.copy(declarationType = importOrExport),
            overThresholdCheck = false,
            _ => Future.successful(incompleteDeclarationJourney.copy(declarationType = importOrExport))
          ))

        result.futureValue mustBe PaymentCalculationController.onPageLoad()
      }

      s"from ${ReviewGoodsController.onPageLoad().url} navigates to ${GoodsOverThresholdController
        .onPageLoad()} if answer No $newOrAmend & $importOrExport and over threshold" in new Navigator {
        val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
        val result: Future[Call] = nextPageWithCallBack(
          RequestWithCallBack(
            ReviewGoodsController.onPageLoad().url,
            No,
            GoodsEntries(if (importOrExport == Import) startedImportGoods else startedExportGoods),
            journey,
            true,
            _ => Future.successful(journey)
          ))

        result.futureValue mustBe GoodsOverThresholdController.onPageLoad()
      }

      s"from ${ReviewGoodsController.onPageLoad().url} navigates to ${GoodsOverThresholdController
        .onPageLoad()} if over for $newOrAmend & $importOrExport" in new Navigator {
        val journey: DeclarationJourney =
          importJourneyWithGoodsOverThreshold.copy(declarationType = importOrExport, journeyType = newOrAmend)
        val result: Future[Call] = nextPageWithCallBack(
          RequestWithCallBack(
            ReviewGoodsController.onPageLoad().url,
            Yes,
            GoodsEntries(if (importOrExport == Import) startedImportGoods else startedExportGoods),
            journey,
            overThresholdCheck = true,
            _ => Future.successful(journey)
          ))

        result.futureValue mustBe GoodsOverThresholdController.onPageLoad()
      }

      "on RemoveGoodsControllerRequest submit" should {
        s"from ${RemoveGoodsController.onPageLoad(1).url} navigates to ${GoodsRemovedController.onPageLoad()} " +
          s"if over for $newOrAmend & $importOrExport Yes and entries are 1" in new Navigator {
          val oneSizeEntries: GoodsEntries = GoodsEntries(if (importOrExport == Import) startedImportGoods else startedExportGoods)
          val journey: DeclarationJourney =
            completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend, goodsEntries = oneSizeEntries)
          val result: Future[Call] = nextPageWithCallBack(
            RemoveGoodsControllerRequest(
              1,
              journey,
              Yes,
              _ => Future.successful(journey)
            ))

          oneSizeEntries.entries.size mustBe 1
          result.futureValue mustBe GoodsRemovedController.onPageLoad()
        }

        s"from ${RemoveGoodsController.onPageLoad(1).url} navigates to ${CheckYourAnswersController.onPageLoad()} " +
          s"if over for $newOrAmend & $importOrExport Yes and entries > 1" in new Navigator {
          val twoSizeEntries: GoodsEntries =
            GoodsEntries(if (importOrExport == Import) completedImportGoods else completedExportGoods)
              .modify(_.entries)
              .using(e => e.+:(e.head))

          val journey: DeclarationJourney =
            completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend, goodsEntries = twoSizeEntries)

          val result: Future[Call] = nextPageWithCallBack(
            RemoveGoodsControllerRequest(
              1,
              journey,
              Yes,
              _ => Future.successful(journey)
            ))

          twoSizeEntries.entries.size mustBe 2
          newOrAmend match {
            case New =>
              journey.declarationRequiredAndComplete mustBe true
              result.futureValue mustBe CheckYourAnswersController.onPageLoad()
            case Amend => result.futureValue mustBe ReviewGoodsController.onPageLoad()
          }
        }
        s"from ${RemoveGoodsController.onPageLoad(1).url} navigates to ${CheckYourAnswersController.onPageLoad()} " +
          s"if over for $newOrAmend & $importOrExport No and completed" in new Navigator {
          val twoSizeEntries: GoodsEntries =
            GoodsEntries(if (importOrExport == Import) completedImportGoods else completedExportGoods)
              .modify(_.entries)
              .using(e => e.+:(e.head))

          val journey: DeclarationJourney =
            completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend, goodsEntries = twoSizeEntries)

          val result: Future[Call] = nextPageWithCallBack(
            RemoveGoodsControllerRequest(
              1,
              journey,
              No,
              _ => Future.successful(journey)
            ))

          twoSizeEntries.entries.size mustBe 2
          newOrAmend match {
            case New =>
              journey.declarationRequiredAndComplete mustBe true
              result.futureValue mustBe CheckYourAnswersController.onPageLoad()
            case Amend => result.futureValue mustBe ReviewGoodsController.onPageLoad()
          }
        }
      }

      s"on $RetrieveDeclarationController submit" should {
        s"navigate to $PreviousDeclarationDetailsController and update if found declaration for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPageWithCallBack(
            RetrieveDeclarationControllerRequest(
              Some(journey.toDeclaration.modify(_.paymentStatus).setTo(Some(Paid))),
              journey,
              _ => Future.successful(journey)
            ))

          result.futureValue mustBe PreviousDeclarationDetailsController.onPageLoad()
        }

        s"navigate to $DeclarationNotFoundController if NOT found declaration for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPageWithCallBack(
            RetrieveDeclarationControllerRequest(
              None,
              journey,
              _ => Future.successful(journey)
            ))
          result.futureValue mustBe DeclarationNotFoundController.onPageLoad()
        }

        s"navigate to $DeclarationNotFoundController if payment status is invalid for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val result = nextPageWithCallBack(
            RetrieveDeclarationControllerRequest(
              Some(journey.toDeclaration.modify(_.paymentStatus).setTo(None)),
              journey,
              _ => Future.successful(journey)
            ))
          if (importOrExport == Import) result.futureValue mustBe DeclarationNotFoundController.onPageLoad()
        }
      }

      s"on $CustomsAgentController submit" should {
        s"navigate to ${AgentDetailsController.onPageLoad()} if Yes for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney
            .copy(declarationType = importOrExport, journeyType = newOrAmend)
          val eventualCall: Future[Call] = nextPageWithCallBack(CustomsAgentRequest(Yes, journey, _ => Future.successful(journey), false))

          eventualCall.futureValue mustBe AgentDetailsController.onPageLoad()
        }

        s"navigate to ${EoriNumberController.onPageLoad()} if No for $newOrAmend & $importOrExport" in new Navigator {
          val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = importOrExport, journeyType = newOrAmend)
          val eventualCall: Future[Call] = nextPageWithCallBack(CustomsAgentRequest(No, journey, _ => Future.successful(journey), false))

          eventualCall.futureValue mustBe EoriNumberController.onPageLoad()
        }
      }
    }
  }

  s"from ${ReviewGoodsController.onPageLoad().url} navigates to ${GoodsOverThresholdController
    .onPageLoad()} if answer No & Export and over threshold" in new Navigator {
    val journey: DeclarationJourney = completedDeclarationJourney.copy(declarationType = Export, journeyType = Amend)
    val result: Future[Call] = nextPageWithCallBack(
      RequestWithCallBack(
        ReviewGoodsController.onPageLoad().url,
        No,
        GoodsEntries(startedExportGoods),
        journey,
        overThresholdCheck = true,
        _ => Future.successful(journey)
      ))

    result.futureValue mustBe GoodsOverThresholdController.onPageLoad()
  }
}

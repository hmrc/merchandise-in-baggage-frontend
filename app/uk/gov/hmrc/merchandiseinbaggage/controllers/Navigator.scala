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

import play.api.mvc.Call
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.service.CurrencyService

import scala.concurrent.{ExecutionContext, Future}

class Navigator {
  import NavigatorMapping._

  def nextPage(request: NavigationRequests): Call = request match {
    case RequestByPass(url)                                    => toNextPage(url)
    case RequestByPassWithIndex(url, idx)                      => nextPageWithIndex(idx)(url)
    case RequestByPassWithIndexAndValue(value, idx)            => valueWeightOfGoodsControllerSubmit(value, idx)
    case RequestWithAnswer(url, value)                         => nextPageWithAnswer(url)(value)
    case RequestWithIndex(url, value, journeyType, idx)        => nextPageWithIndex(url)(value, journeyType, idx)
    case RequestWithDeclarationType(url, declarationType, idx) => nextPageWithIndexAndDeclarationType(declarationType, idx)(url)
  }

  def nextPageWithCallBack(request: NavigationRequestsAsync)(implicit ec: ExecutionContext): Future[Call] =
    request match {
      case r: RequestWithCallBack => reviewGoodsController(r.value, r.declarationJourney, r.overThresholdCheck, r.callBack)
      case r: RequestWithIndexAndCallBack =>
        purchaseDetailsController(r.purchaseDetailsInput, r.index, r.journey, r.goodsEntry, r.callBack)
      case RemoveGoodsControllerRequest(idx, journey, value, upsert) =>
        removeGoodOrRedirect(idx, journey, value, upsert)
      case RetrieveDeclarationControllerRequest(declaration, journey, upsert) =>
        retrieveDeclarationControllerSubmit(declaration, journey, upsert)
      case VehicleRegistrationNumberControllerRequest(journey, regNumber, upsert) =>
        vehicleRegistrationNumberControllerSubmit(journey, regNumber, upsert)
    }
}

object NavigatorMapping {

  val toNextPage: Map[String, Call] = Map(
    AgentDetailsController.onPageLoad().url               -> EnterAgentAddressController.onPageLoad(),
    EnterEmailController.onPageLoad().url                 -> JourneyDetailsController.onPageLoad(),
    EoriNumberController.onPageLoad().url                 -> TravellerDetailsController.onPageLoad(),
    JourneyDetailsController.onPageLoad().url             -> GoodsInVehicleController.onPageLoad(),
    PreviousDeclarationDetailsController.onPageLoad().url -> ExciseAndRestrictedGoodsController.onPageLoad(),
    TravellerDetailsController.onPageLoad().url           -> EnterEmailController.onPageLoad(),
  )

  def nextPageWithAnswer[T]: Map[String, T => Call] = Map(
    GoodsDestinationController.onPageLoad().url -> goodsDestination,
    CustomsAgentController.onPageLoad().url     -> customsAgent,
    GoodsInVehicleController.onPageLoad().url   -> goodsInVehicleController,
    NewOrExistingController.onPageLoad().url    -> newOrExistingController,
    VehicleSizeController.onPageLoad().url      -> vehicleSizeControllerSubmit
  )

  val nextPageWithIndex: Map[String, (YesNo, JourneyType, Int) => Call] = Map(
    ExciseAndRestrictedGoodsController.onPageLoad().url -> exciseAndRestrictedGoods
  )

  def nextPageWithIndex(idx: Int): Map[String, Call] = Map(
    GoodsOriginController.onPageLoad(idx).url        -> PurchaseDetailsController.onPageLoad(idx),
    GoodsVatRateController.onPageLoad(idx).url       -> SearchGoodsCountryController.onPageLoad(idx),
    SearchGoodsCountryController.onPageLoad(idx).url -> PurchaseDetailsController.onPageLoad(idx)
  )

  def nextPageWithIndexAndDeclarationType(declarationType: DeclarationType, idx: Int): Map[String, Call] = Map(
    GoodsTypeQuantityController.onPageLoad(idx).url -> goodsTypeQuantityController(declarationType, idx),
  )

  def valueWeightOfGoodsControllerSubmit(belowThreshold: YesNo, entriesSize: Int): Call = belowThreshold match {
    case No  => CannotUseServiceController.onPageLoad()
    case Yes => GoodsTypeQuantityController.onPageLoad(entriesSize)
  }

  def vehicleRegistrationNumberControllerSubmit(
    journey: DeclarationJourney,
    vehicleReg: String,
    upsert: DeclarationJourney => Future[DeclarationJourney])(implicit ec: ExecutionContext): Future[Call] =
    upsert(journey.copy(maybeRegistrationNumber = Some(vehicleReg)))
      .map { _ =>
        CheckYourAnswersController.onPageLoad()
      }

  private def vehicleSizeControllerSubmit[T](isSmallVehicle: T): Call = isSmallVehicle match {
    case Yes => VehicleRegistrationNumberController.onPageLoad()
    case No  => CannotUseServiceController.onPageLoad()
  }

  private def exciseAndRestrictedGoods(value: YesNo, journeyType: JourneyType, idx: Int): Call =
    value match {
      case Yes => CannotUseServiceController.onPageLoad()
      case No  => ValueWeightOfGoodsController.onPageLoad()
    }

  private def customsAgent[T](value: T): Call = value match {
    case Yes => AgentDetailsController.onPageLoad()
    case No  => EoriNumberController.onPageLoad()
  }

  private def goodsDestination[T](value: T) =
    value match {
      case NorthernIreland => CannotUseServiceIrelandController.onPageLoad()
      case GreatBritain    => ExciseAndRestrictedGoodsController.onPageLoad()
    }

  private def goodsInVehicleController[T](value: T) =
    value match {
      case Yes => VehicleSizeController.onPageLoad()
      case No  => CheckYourAnswersController.onPageLoad()
    }

  private def goodsTypeQuantityController(declarationType: DeclarationType, idx: Int): Call =
    declarationType match {
      case Import => GoodsVatRateController.onPageLoad(idx)
      case Export => SearchGoodsCountryController.onPageLoad(idx)
    }

  private def newOrExistingController[T](journeyType: T): Call =
    journeyType match {
      case New   => GoodsDestinationController.onPageLoad()
      case Amend => RetrieveDeclarationController.onPageLoad()
    }

  def reviewGoodsController(
    declareMoreGoods: YesNo,
    declarationJourney: DeclarationJourney,
    overThresholdCheck: Boolean,
    upsert: DeclarationJourney => Future[DeclarationJourney])(implicit ec: ExecutionContext): Future[Call] =
    if (overThresholdCheck) Future.successful(GoodsOverThresholdController.onPageLoad())
    else {
      val redirectToCya: Boolean = (declarationJourney.declarationType, declarationJourney.journeyType) match {
        case (Export, New)   => declarationJourney.declarationRequiredAndComplete
        case (Export, Amend) => declarationJourney.amendmentRequiredAndComplete
        case (Import, _)     => false
      }

      (redirectToCya, declareMoreGoods) match {
        case (_, Yes)    => updateEntriesAndRedirect(declarationJourney, upsert)
        case (false, No) => Future.successful(PaymentCalculationController.onPageLoad())
        case (true, No)  => Future.successful(CheckYourAnswersController.onPageLoad())
      }
    }

  private def updateEntriesAndRedirect(declarationJourney: DeclarationJourney, upsert: DeclarationJourney => Future[DeclarationJourney])(
    implicit ec: ExecutionContext): Future[Call] =
    upsert(declarationJourney.updateGoodsEntries()).map { _ =>
      GoodsTypeQuantityController.onPageLoad(declarationJourney.updateGoodsEntries().goodsEntries.entries.size)
    }

  def purchaseDetailsController(
    purchaseDetailsInput: PurchaseDetailsInput,
    idx: Int,
    declarationJourney: DeclarationJourney,
    goodsEntry: GoodsEntry,
    upsert: DeclarationJourney => Future[DeclarationJourney])(implicit ec: ExecutionContext): Future[Call] =
    CurrencyService
      .getCurrencyByCode(purchaseDetailsInput.currency)
      .fold(Future(CannotAccessPageController.onPageLoad())) { currency =>
        val updatedGoodsEntry: GoodsEntry = updateGoodsEntry(purchaseDetailsInput.price, currency, goodsEntry)
        val updatedDeclarationJourney =
          declarationJourney.copy(goodsEntries = declarationJourney.goodsEntries.patch(idx, updatedGoodsEntry))

        upsert(updatedDeclarationJourney).map { _ =>
          ReviewGoodsController.onPageLoad()
        }
      }

  def updateGoodsEntry(amount: String, currency: Currency, goodsEntry: GoodsEntry): GoodsEntry =
    goodsEntry match {
      case entry: ImportGoodsEntry => entry.copy(maybePurchaseDetails = Some(PurchaseDetails(amount, currency)))
      case entry: ExportGoodsEntry => entry.copy(maybePurchaseDetails = Some(PurchaseDetails(amount, currency)))
    }

  def removeGoodOrRedirect(
    idx: Int,
    declarationJourney: DeclarationJourney,
    removeGoods: YesNo,
    upsert: DeclarationJourney => Future[DeclarationJourney])(implicit ec: ExecutionContext): Future[Call] =
    removeGoods match {
      case Yes =>
        upsert(declarationJourney.copy(goodsEntries = declarationJourney.goodsEntries.remove(idx)))
          .flatMap { _ =>
            redirectIfGoodRemoved(declarationJourney)
          }
      case No =>
        backToCheckYourAnswersIfJourneyCompleted(declarationJourney)
    }

  private def redirectIfGoodRemoved(declarationJourney: DeclarationJourney): Future[Call] =
    if (declarationJourney.goodsEntries.entries.size == 1)
      Future successful GoodsRemovedController.onPageLoad()
    else backToCheckYourAnswersIfJourneyCompleted(declarationJourney)

  private def backToCheckYourAnswersIfJourneyCompleted(declarationJourney: DeclarationJourney): Future[Call] =
    if (declarationJourney.declarationRequiredAndComplete)
      Future successful CheckYourAnswersController.onPageLoad()
    else Future successful ReviewGoodsController.onPageLoad()

  def retrieveDeclarationControllerSubmit(
    maybeDeclaration: Option[Declaration],
    declarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney])(implicit ec: ExecutionContext): Future[Call] =
    maybeDeclaration match {
      case Some(declaration) if isValid(declaration) =>
        upsert(
          declarationJourney
            .copy(declarationType = declaration.declarationType, declarationId = declaration.declarationId)) map { _ =>
          PreviousDeclarationDetailsController.onPageLoad()
        }
      case _ => Future successful DeclarationNotFoundController.onPageLoad()
    }

  private def isValid(declaration: Declaration) =
    declaration.declarationType match {
      case Export => true
      case Import => declaration.paymentStatus.contains(Paid) || declaration.paymentStatus.contains(NotRequired)
    }
}

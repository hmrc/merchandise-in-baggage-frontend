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
    case RequestByPassWithIndex(url, idx) => nextPageWithIndex(idx)(url)
  }

  def nextPageWithCallBack(request: NavigationRequestsAsync)(implicit ec: ExecutionContext): Future[Call] = request match {
    case ReviewGoodsRequest(value, journey, overThresholdCheck, upsert) => reviewGoods(value, journey, overThresholdCheck, upsert)
    case r: RequestWithIndexAndCallBack =>
      purchaseDetails(r.purchaseDetailsInput, r.index, r.journey, r.goodsEntry, r.callBack)

    case RemoveGoodsRequest(idx, journey, value, upsert)                   => removeGoodOrRedirect(idx, journey, value, upsert)
    case RetrieveDeclarationRequest(declaration, journey, upsert)          => retrieveDeclaration(declaration, journey, upsert)
    case VehicleRegistrationNumberRequest(journey, regNumber, upsert)      => vehicleRegistrationNumber(journey, regNumber, upsert)
    case CustomsAgentRequest(value, journey, upsert, complete)             => customsAgent(value, journey, upsert, complete)
    case EnterEmailRequest(journey, upsert, complete)                      => enterEmail(journey, upsert, complete)
    case EoriNumberRequest(journey, upsert, complete)                      => enterEori(journey, upsert, complete)
    case ExciseAndRestrictedGoodsRequest(value, journey, upsert, complete) => exciseAndRestrictedGoods(value, journey, upsert, complete)
    case GoodsDestinationRequest(value, journey, upsert, complete)         => goodsDestination(value, journey, upsert, complete)
    case GoodsInVehicleRequest(value, journey, upsert, complete)           => goodsInVehicleController(value, journey, upsert, complete)
    case JourneyDetailsRequest(journey, upsert, complete)                  => journeyDetails(journey, upsert, complete)
    case TravellerDetailsRequest(journey, upsert, complete)                => travellerDetails(journey, upsert, complete)
    case ValueWeightOfGoodsRequest(value, idx, journey, upsert, complete)  => valueWeightOfGoods(value, idx, journey, upsert, complete)
    case VehicleSizeRequest(value, journey, upsert, complete)              => vehicleSizeController(value, journey, upsert, complete)
    case NewOrExistingRequest(journey, upsert, complete)                   => newOrExisting(journey, upsert, complete)
    case AgentDetailsRequest(agentName, journey, upsert)                   => agentDetails(agentName, journey, upsert)
    case PreviousDeclarationDetailsRequest(journey, declaration, upsert)   => previousDeclarationDetails(journey, declaration, upsert)
    case GoodsTypeQuantityRequest(declarationType, idx)                    => goodsTypeQuantity(declarationType, idx)
  }
}

object NavigatorMapping {

  def nextPageWithIndex(idx: Int): Map[String, Call] = Map(
    GoodsOriginController.onPageLoad(idx).url        -> PurchaseDetailsController.onPageLoad(idx),
    GoodsVatRateController.onPageLoad(idx).url       -> SearchGoodsCountryController.onPageLoad(idx),
    SearchGoodsCountryController.onPageLoad(idx).url -> PurchaseDetailsController.onPageLoad(idx)
  )

  def vehicleRegistrationNumber(journey: DeclarationJourney, vehicleReg: String, upsert: DeclarationJourney => Future[DeclarationJourney])(
    implicit ec: ExecutionContext): Future[Call] =
    upsert(journey.copy(maybeRegistrationNumber = Some(vehicleReg)))
      .map { _ =>
        CheckYourAnswersController.onPageLoad()
      }

  def agentDetails(agentName: String, journey: DeclarationJourney, upsert: DeclarationJourney => Future[DeclarationJourney])(
    implicit ec: ExecutionContext): Future[Call] =
    upsert(journey.copy(maybeCustomsAgentName = Some(agentName)))
      .map(_ => EnterAgentAddressController.onPageLoad())

  def previousDeclarationDetails(
    journey: DeclarationJourney,
    originalDeclaration: Declaration,
    upsert: DeclarationJourney => Future[DeclarationJourney])(implicit ec: ExecutionContext): Future[Call] = {
    val updatedDeclaration =
      DeclarationJourney(journey.sessionId, originalDeclaration.declarationType)
        .copy(
          declarationId = originalDeclaration.declarationId,
          journeyType = Amend,
          maybeGoodsDestination = Some(originalDeclaration.goodsDestination),
          maybeRetrieveDeclaration = journey.maybeRetrieveDeclaration
        )

    upsert(updatedDeclaration).map(_ => ExciseAndRestrictedGoodsController.onPageLoad())
  }

  def vehicleSizeController(
    value: YesNo,
    updatedDeclarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney],
    declarationRequiredAndComplete: Boolean)(implicit ec: ExecutionContext): Future[Call] = {
    val redirectTo = value match {
      case Yes => VehicleRegistrationNumberController.onPageLoad()
      case No  => CannotUseServiceController.onPageLoad()
    }
    persistAndRedirect(updatedDeclarationJourney, declarationRequiredAndComplete, redirectTo, upsert)
  }

  def exciseAndRestrictedGoods(
    value: YesNo,
    updatedDeclarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney],
    declarationRequiredAndComplete: Boolean)(implicit ec: ExecutionContext): Future[Call] = {
    val redirectTo = value match {
      case Yes => CannotUseServiceController.onPageLoad()
      case No  => ValueWeightOfGoodsController.onPageLoad()
    }
    persistAndRedirect(updatedDeclarationJourney, declarationRequiredAndComplete, redirectTo, upsert)
  }

  def valueWeightOfGoods(
    value: YesNo,
    entriesSize: Int,
    updatedDeclarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney],
    declarationRequiredAndComplete: Boolean)(implicit ec: ExecutionContext): Future[Call] = {
    val redirectTo = value match {
      case No  => CannotUseServiceController.onPageLoad()
      case Yes => GoodsTypeQuantityController.onPageLoad(entriesSize)
    }
    persistAndRedirect(updatedDeclarationJourney, declarationRequiredAndComplete, redirectTo, upsert)
  }

  def customsAgent(
    value: YesNo,
    updatedDeclarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney],
    declarationRequiredAndComplete: Boolean)(implicit ec: ExecutionContext): Future[Call] = {
    val redirectTo = value match {
      case Yes => AgentDetailsController.onPageLoad()
      case No  => EoriNumberController.onPageLoad()
    }
    persistAndRedirect(updatedDeclarationJourney, declarationRequiredAndComplete, redirectTo, upsert)
  }

  def enterEmail(
    updatedDeclarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney],
    declarationRequiredAndComplete: Boolean)(implicit ec: ExecutionContext): Future[Call] =
    persistAndRedirect(updatedDeclarationJourney, declarationRequiredAndComplete, JourneyDetailsController.onPageLoad(), upsert)

  def enterEori(
    updatedDeclarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney],
    declarationRequiredAndComplete: Boolean)(implicit ec: ExecutionContext): Future[Call] =
    persistAndRedirect(updatedDeclarationJourney, declarationRequiredAndComplete, TravellerDetailsController.onPageLoad(), upsert)

  def goodsDestination(
    value: GoodsDestination,
    updatedDeclarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney],
    declarationRequiredAndComplete: Boolean)(implicit ec: ExecutionContext): Future[Call] = {
    val redirectTo = value match {
      case NorthernIreland => CannotUseServiceIrelandController.onPageLoad()
      case GreatBritain    => ExciseAndRestrictedGoodsController.onPageLoad()
    }
    persistAndRedirect(updatedDeclarationJourney, declarationRequiredAndComplete, redirectTo, upsert)
  }

  def goodsInVehicleController(
    value: YesNo,
    updatedDeclarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney],
    declarationRequiredAndComplete: Boolean)(implicit ec: ExecutionContext): Future[Call] = {
    val redirectTo = value match {
      case Yes => VehicleSizeController.onPageLoad()
      case No  => CheckYourAnswersController.onPageLoad()
    }
    persistAndRedirect(updatedDeclarationJourney, declarationRequiredAndComplete, redirectTo, upsert)
  }

  def journeyDetails(
    updatedDeclarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney],
    declarationRequiredAndComplete: Boolean)(implicit ec: ExecutionContext): Future[Call] =
    persistAndRedirect(updatedDeclarationJourney, declarationRequiredAndComplete, GoodsInVehicleController.onPageLoad(), upsert)

  def travellerDetails(
    updatedDeclarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney],
    declarationRequiredAndComplete: Boolean)(implicit ec: ExecutionContext): Future[Call] =
    persistAndRedirect(updatedDeclarationJourney, declarationRequiredAndComplete, EnterEmailController.onPageLoad(), upsert)

  def newOrExisting(
    updatedDeclarationJourney: DeclarationJourney,
    upsert: DeclarationJourney => Future[DeclarationJourney],
    declarationRequiredAndComplete: Boolean)(implicit ec: ExecutionContext): Future[Call] = {
    val redirectTo = updatedDeclarationJourney.journeyType match {
      case New   => GoodsDestinationController.onPageLoad()
      case Amend => RetrieveDeclarationController.onPageLoad()
    }
    persistAndRedirect(updatedDeclarationJourney, declarationRequiredAndComplete, redirectTo, upsert)
  }

  private def persistAndRedirect(
    updatedDeclarationJourney: DeclarationJourney,
    declarationRequiredAndComplete: Boolean,
    redirectIfNotComplete: Call,
    upsert: DeclarationJourney => Future[DeclarationJourney])(implicit ec: ExecutionContext): Future[Call] =
    upsert(updatedDeclarationJourney).map { _ =>
      if (declarationRequiredAndComplete) CheckYourAnswersController.onPageLoad()
      else redirectIfNotComplete
    }

  def goodsTypeQuantity(declarationType: DeclarationType, idx: Int)(implicit ec: ExecutionContext): Future[Call] = Future {
    declarationType match {
      case Import => GoodsVatRateController.onPageLoad(idx)
      case Export => SearchGoodsCountryController.onPageLoad(idx)
    }
  }

  def reviewGoods(
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

  def purchaseDetails(
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

  def retrieveDeclaration(
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
      case _ =>
        upsert(declarationJourney) map { _ =>
          DeclarationNotFoundController.onPageLoad()
        }
    }

  private def isValid(declaration: Declaration) =
    declaration.declarationType match {
      case Export => true
      case Import => declaration.paymentStatus.contains(Paid) || declaration.paymentStatus.contains(NotRequired)
    }
}

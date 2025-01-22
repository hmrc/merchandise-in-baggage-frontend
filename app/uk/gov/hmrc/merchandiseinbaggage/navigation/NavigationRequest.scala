/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.navigation

import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.ThresholdCheck
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, GoodsDestination, SessionId, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntry, ImportExportChoice, PurchaseDetailsInput}

import scala.concurrent.Future

sealed trait NavigationRequest

final case class ReviewGoodsRequest(
  value: YesNo,
  declarationJourney: DeclarationJourney,
  overThresholdCheck: ThresholdCheck,
  callBack: DeclarationJourney => Future[DeclarationJourney]
) extends NavigationRequest

final case class PurchaseDetailsRequest(
  purchaseDetailsInput: PurchaseDetailsInput,
  index: Int,
  goodsEntry: GoodsEntry,
  journey: DeclarationJourney,
  callBack: DeclarationJourney => Future[DeclarationJourney]
) extends NavigationRequest

final case class RemoveGoodsRequest(
  idx: Int,
  declarationJourney: DeclarationJourney,
  removeGoods: YesNo,
  upsert: DeclarationJourney => Future[DeclarationJourney]
) extends NavigationRequest

final case class RetrieveDeclarationRequest(
  declaration: Option[Declaration],
  declarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney]
) extends NavigationRequest

final case class VehicleRegistrationNumberRequest(
  declarationJourney: DeclarationJourney,
  regNumber: String,
  upsert: DeclarationJourney => Future[DeclarationJourney]
) extends NavigationRequest

//New Request to depracate all of above
final case class CustomsAgentRequest(
  answer: YesNo,
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  declarationRequiredAndComplete: Boolean
) extends NavigationRequest

final case class EnterEmailRequest(
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  declarationRequiredAndComplete: Boolean
) extends NavigationRequest

final case class EoriNumberRequest(
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  declarationRequiredAndComplete: Boolean
) extends NavigationRequest

final case class ExciseAndRestrictedGoodsRequest(
  answer: YesNo,
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  declarationRequiredAndComplete: Boolean
) extends NavigationRequest

final case class GoodsDestinationRequest(
  answer: GoodsDestination,
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  declarationRequiredAndComplete: Boolean
) extends NavigationRequest

final case class GoodsInVehicleRequest(
  answer: YesNo,
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  declarationRequiredAndComplete: Boolean
) extends NavigationRequest

final case class JourneyDetailsRequest(
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  declarationRequiredAndComplete: Boolean
) extends NavigationRequest

final case class TravellerDetailsRequest(
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  declarationRequiredAndComplete: Boolean
) extends NavigationRequest

final case class ValueWeightOfGoodsRequest(
  value: YesNo,
  idx: Int,
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  declarationRequiredAndComplete: Boolean
) extends NavigationRequest

final case class VehicleSizeRequest(
  value: YesNo,
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  declarationRequiredAndComplete: Boolean
) extends NavigationRequest

final case class ImportExportChoiceRequest(
  choice: ImportExportChoice,
  sessionId: SessionId,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  isAssistedDigital: Boolean
) extends NavigationRequest

final case class NewOrExistingRequest(
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  declarationRequiredAndComplete: Boolean
) extends NavigationRequest

final case class AgentDetailsRequest(
  agentName: String,
  updatedDeclarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney]
) extends NavigationRequest

final case class PreviousDeclarationDetailsRequest(
  journey: DeclarationJourney,
  originalDeclaration: Declaration,
  upsert: DeclarationJourney => Future[DeclarationJourney],
  isAssistedDigital: Boolean
) extends NavigationRequest

final case class GoodsTypeRequest(
  journey: DeclarationJourney,
  entries: GoodsEntry,
  idx: Int,
  category: String,
  upsert: DeclarationJourney => Future[DeclarationJourney]
) extends NavigationRequest

final case class SearchGoodsCountryRequest(
  journey: DeclarationJourney,
  entries: GoodsEntry,
  idx: Int,
  upsert: DeclarationJourney => Future[DeclarationJourney]
) extends NavigationRequest

final case class GoodsVatRateRequest(
  journey: DeclarationJourney,
  entries: GoodsEntry,
  idx: Int,
  upsert: DeclarationJourney => Future[DeclarationJourney]
) extends NavigationRequest

final case class GoodsOriginRequest(
  journey: DeclarationJourney,
  entries: GoodsEntry,
  idx: Int,
  upsert: DeclarationJourney => Future[DeclarationJourney]
) extends NavigationRequest

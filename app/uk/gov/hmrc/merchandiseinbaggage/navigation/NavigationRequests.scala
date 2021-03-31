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

package uk.gov.hmrc.merchandiseinbaggage.navigation

import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationType, JourneyType, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries, GoodsEntry, PurchaseDetailsInput}

import scala.concurrent.Future

sealed trait NavigationRequests
final case class RequestByPass(currentUrl: String) extends NavigationRequests
final case class RequestByPassWithIndex(currentUrl: String, idx: Int) extends NavigationRequests
final case class RequestByPassWithIndexAndValue(value: YesNo, idx: Int) extends NavigationRequests
final case class RequestWithAnswer[T](currentUrl: String, value: T) extends NavigationRequests
final case class RequestWithIndex(currentUrl: String, value: YesNo, journeyType: JourneyType, idx: Int) extends NavigationRequests
final case class RequestWithDeclarationType(currentUrl: String, declarationType: DeclarationType, idx: Int) extends NavigationRequests

sealed trait NavigationRequestsAsync
final case class RequestWithCallBack(
  currentUrl: String,
  value: YesNo,
  updatedGoodsEntries: GoodsEntries,
  declarationJourney: DeclarationJourney,
  overThresholdCheck: Boolean,
  callBack: DeclarationJourney => Future[DeclarationJourney])
    extends NavigationRequestsAsync

final case class RequestWithIndexAndCallBack(
  purchaseDetailsInput: PurchaseDetailsInput,
  index: Int,
  goodsEntry: GoodsEntry,
  journey: DeclarationJourney,
  callBack: DeclarationJourney => Future[DeclarationJourney])
    extends NavigationRequestsAsync

final case class RemoveGoodsControllerRequest(
  idx: Int,
  declarationJourney: DeclarationJourney,
  removeGoods: YesNo,
  upsert: DeclarationJourney => Future[DeclarationJourney])
    extends NavigationRequestsAsync

final case class RetrieveDeclarationControllerRequest(
  declaration: Option[Declaration],
  declarationJourney: DeclarationJourney,
  upsert: DeclarationJourney => Future[DeclarationJourney])
    extends NavigationRequestsAsync

final case class VehicleRegistrationNumberControllerRequest(
  declarationJourney: DeclarationJourney,
  regNumber: String,
  upsert: DeclarationJourney => Future[DeclarationJourney])
    extends NavigationRequestsAsync

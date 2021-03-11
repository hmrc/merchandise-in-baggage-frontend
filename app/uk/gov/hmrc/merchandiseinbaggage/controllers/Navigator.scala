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
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes.{CustomsAgentController, _}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.NorthernIreland
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.Yes
import uk.gov.hmrc.merchandiseinbaggage.model.api.{JourneyType, YesNo}

sealed trait NavigationRequests
final case class RequestByPass(currentUrl: String) extends NavigationRequests
final case class RequestByPassWithIndex(currentUrl: String, idx: Int) extends NavigationRequests
final case class RequestWithAnswer[T](currentUrl: String, value: T) extends NavigationRequests
final case class RequestWithIndex(currentUrl: String, value: YesNo, journeyType: JourneyType, idx: Int) extends NavigationRequests

class Navigator {

  def nextPage(request: NavigationRequests): Call = request match {
    case RequestByPass(url)                             => Navigator.nextPage(url)
    case RequestByPassWithIndex(url, idx)               => Navigator.nextPageWithIndex(idx)(url)
    case RequestWithAnswer(url, value)                  => Navigator.nextPageWithAnswer(url)(value)
    case RequestWithIndex(url, value, journeyType, idx) => Navigator.nextPageWithIndex(url)(value, journeyType, idx)
  }
}

object Navigator {

  val nextPage: Map[String, Call] = Map(
    AgentDetailsController.onPageLoad().url -> EnterAgentAddressController.onPageLoad(),
    EnterEmailController.onPageLoad().url   -> JourneyDetailsController.onPageLoad(),
    EoriNumberController.onPageLoad().url   -> TravellerDetailsController.onPageLoad(),
  )

  def nextPageWithAnswer[T]: Map[String, T => Call] = Map(
    GoodsDestinationController.onPageLoad().url -> goodsDestination,
    CustomsAgentController.onPageLoad().url     -> customsAgent,
    GoodsInVehicleController.onPageLoad().url   -> goodsInVehicleController,
  )

  val nextPageWithIndex: Map[String, (YesNo, JourneyType, Int) => Call] = Map(
    ExciseAndRestrictedGoodsController.onPageLoad().url -> exciseAndRestrictedGoods
  )

  def nextPageWithIndex(idx: Int): Map[String, Call] = Map(
    GoodsOriginController.onPageLoad(idx).url -> PurchaseDetailsController.onPageLoad(idx)
  )

  private def exciseAndRestrictedGoods(value: YesNo, journeyType: JourneyType, idx: Int): Call =
    (value, journeyType) match {
      case (Yes, _)   => CannotUseServiceController.onPageLoad()
      case (_, New)   => ValueWeightOfGoodsController.onPageLoad()
      case (_, Amend) => GoodsTypeQuantityController.onPageLoad(idx)
    }

  private def customsAgent[T](value: T): Call =
    if (value == Yes) AgentDetailsController.onPageLoad()
    else EoriNumberController.onPageLoad()

  private def goodsDestination[T](value: T) =
    if (value == NorthernIreland) routes.CannotUseServiceIrelandController.onPageLoad()
    else ExciseAndRestrictedGoodsController.onPageLoad()

  private def goodsInVehicleController[T](value: T) =
    if (value == Yes) VehicleSizeController.onPageLoad()
    else CheckYourAnswersController.onPageLoad()
}

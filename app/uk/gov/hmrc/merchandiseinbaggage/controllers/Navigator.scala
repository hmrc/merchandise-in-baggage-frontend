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
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.Yes
import uk.gov.hmrc.merchandiseinbaggage.model.api.{JourneyType, YesNo}

sealed trait NavigationRequests
final case class RequestByPass(currentUrl: String) extends NavigationRequests
final case class RequestWithYesNo(currentUrl: String, value: YesNo) extends NavigationRequests
final case class RequestWithIndex(currentUrl: String, value: YesNo, journeyType: JourneyType, idx: Int) extends NavigationRequests

class Navigator {

  def nextPage(request: NavigationRequests): Call = request match {
    case RequestByPass(url)                             => Navigator.pages0(url)
    case RequestWithYesNo(url, value)                   => Navigator.pages1(url)(value)
    case RequestWithIndex(url, value, journeyType, idx) => Navigator.pages3(url)(idx, journeyType, value)
  }
}

object Navigator {

  def pages0: Map[String, Call] = Map(
    AgentDetailsController.onPageLoad().url -> EnterAgentAddressController.onPageLoad()
  )

  def pages1: Map[String, YesNo => Call] = Map(
    CustomsAgentController.onPageLoad().url -> customsAgent
  )

  def pages3: Map[String, (Int, JourneyType, YesNo) => Call] = Map(
    ExciseAndRestrictedGoodsController.onPageLoad().url -> exciseAndRestrictedGoods
  )

  private def exciseAndRestrictedGoods(idx: Int, journeyType: JourneyType, value: YesNo): Call =
    (value, journeyType) match {
      case (Yes, _)   => CannotUseServiceController.onPageLoad()
      case (_, New)   => ValueWeightOfGoodsController.onPageLoad()
      case (_, Amend) => GoodsTypeQuantityController.onPageLoad(idx)
    }

  private def customsAgent(value: YesNo): Call =
    if (value == Yes) AgentDetailsController.onPageLoad()
    else EoriNumberController.onPageLoad()
}

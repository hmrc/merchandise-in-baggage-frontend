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

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.goodsDestinationUnansweredMessage
import uk.gov.hmrc.merchandiseinbaggage.views.html.CannotUseServiceView

@Singleton
class CannotUseServiceController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  view: CannotUseServiceView)(implicit val appConfig: AppConfig)
    extends DeclarationJourneyController {

  private val backUrl = Call("GET", "#")

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]): Call = {
    val referer: String = request.headers.get(REFERER).getOrElse("")
    if (referer.contains(routes.ExciseAndRestrictedGoodsController.onPageLoad().url)) routes.ExciseAndRestrictedGoodsController.onPageLoad()
    else if (referer.contains(routes.ValueWeightOfGoodsController.onPageLoad().url)) routes.ValueWeightOfGoodsController.onPageLoad()
    else if (referer.contains(routes.VehicleSizeController.onPageLoad().url)) routes.VehicleSizeController.onPageLoad()
    else backUrl
  }

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    request.declarationJourney.maybeGoodsDestination
      .fold(actionProvider.invalidRequest(goodsDestinationUnansweredMessage)) { goodsDestination =>
        Ok(view(request.declarationJourney.declarationType, goodsDestination, backButtonUrl))
      }
  }
}

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

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import javax.inject.Inject
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.SkeletonPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

/**
 * Controller serving skeleton pages
 *
 * It is envisaged this controller will be re-factored away action-by-action as we build out the journey
 */
class SkeletonJourneyController @Inject()(mcc: MessagesControllerComponents,
                                          actionProvider: DeclarationJourneyActionProvider,
                                          page: SkeletonPage)
                                         (implicit val ec: ExecutionContext, appConfig: AppConfig)
  extends FrontendController(mcc) {
  implicit def messages(implicit request: Request[_]): Messages = controllerComponents.messagesApi.preferred(request)

  val selectDeclarationType: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(page(s"selectDeclarationType.title", routes.ExciseAndRestrictedGoodsController.onPageLoad()))
  }

  val searchGoods: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(page(s"searchGoods.title", routes.SearchGoodsCountryController.onPageLoad()))
  }

  val purchaseDetails: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(page(s"purchaseDetails.title", routes.ReviewGoodsController.onPageLoad()))
  }

  val taxCalculation: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(page(s"taxCalculation.title", routes.SkeletonJourneyController.customsAgent()))
  }

  val customsAgent: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(page(s"customsAgent.title", routes.TraderDetailsController.onPageLoad()))
  }

  val enterTraderAddress: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(page(s"enterTraderAddress.title", routes.SkeletonJourneyController.selectTraderAddress()))
  }

  val selectTraderAddress: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(page(s"selectTraderAddress.title", routes.SkeletonJourneyController.enterEoriNumber()))
  }

  val enterEoriNumber: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(page(s"enterEoriNumber.title", routes.SkeletonJourneyController.traderJourneyDetails()))
  }

  val traderJourneyDetails: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(page(s"traderJourneyDetails.title", routes.CheckYourAnswersController.onPageLoad()))
  }
}

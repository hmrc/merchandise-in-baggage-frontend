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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.SkeletonPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

/**
 * Controller serving skeleton pages
 *
 * It is envisaged this controller will be re-factored away action-by-action as we build out the journey
 */
class SkeletonJourneyController @Inject()(mcc: MessagesControllerComponents, page: SkeletonPage)
                                         (implicit val ec: ExecutionContext, appConfig: AppConfig)
  extends FrontendController(mcc) {

  val selectDeclarationType: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"selectDeclarationType.title", routes.ExciseAndRestrictedGoodsController.onPageLoad()))
  }

  val valueWeightOfGoods: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"valueWeightOfGoods.title", routes.SkeletonJourneyController.searchGoods()))
  }

  val searchGoods: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"searchGoods.title", routes.SkeletonJourneyController.searchGoodsCountry()))
  }

  val searchGoodsCountry: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"searchGoodsCountry.title", routes.SkeletonJourneyController.purchaseDetails()))
  }

  val purchaseDetails: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"purchaseDetails.title", routes.SkeletonJourneyController.reviewGoods()))
  }

  val reviewGoods: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"reviewGoods.title", routes.SkeletonJourneyController.taxCalculation()))
  }

  val taxCalculation: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"taxCalculation.title", routes.SkeletonJourneyController.customsAgent()))
  }

  val customsAgent: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"customsAgent.title", routes.SkeletonJourneyController.traderDetails()))
  }

  val traderDetails: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"traderDetails.title", routes.SkeletonJourneyController.enterTraderAddress()))
  }

  val enterTraderAddress: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"enterTraderAddress.title", routes.SkeletonJourneyController.selectTraderAddress()))
  }

  val selectTraderAddress: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"selectTraderAddress.title", routes.SkeletonJourneyController.enterEoriNumber()))
  }

  val enterEoriNumber: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"enterEoriNumber.title", routes.SkeletonJourneyController.traderJourneyDetails()))
  }

  val traderJourneyDetails: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"traderJourneyDetails.title", routes.CheckYourAnswersController.onPageLoad()))
  }
}

/*
 * Copyright 2020 HM Revenue & Customs
 *
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

  val start: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"start.title", routes.SkeletonJourneyController.selectDeclarationType()))
  }

  val selectDeclarationType: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"selectDeclarationType.title", routes.SkeletonJourneyController.exciseAndRestrictedGoods()))
  }

  val exciseAndRestrictedGoods: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"exciseAndRestrictedGoods.title", routes.GoodsDestinationController.onPageLoad()))
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

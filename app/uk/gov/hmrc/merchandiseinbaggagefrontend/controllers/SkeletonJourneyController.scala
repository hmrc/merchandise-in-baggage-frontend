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
    Ok(page(s"start.title", routes.SkeletonJourneyController.importExport()))
  }

  val importExport: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"importExport.title", routes.SkeletonJourneyController.multipleCountriesEuCheck()))
  }

  val multipleCountriesEuCheck: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"multipleCountriesEuCheck.title", routes.SkeletonJourneyController.goodsExcise()))
  }

  val goodsExcise: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"goodsExcise.title", routes.SkeletonJourneyController.valueWeightOfGoods()))
  }

  val valueWeightOfGoods: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"valueWeightOfGoods.title", routes.SkeletonJourneyController.goodsType()))
  }

  val goodsType: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"goodsType.title", routes.SkeletonJourneyController.goodsCategory()))
  }

  val goodsCategory: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"goodsCategory.title", routes.SkeletonJourneyController.goodsDetailsWhere()))
  }

  val goodsDetailsWhere: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"goodsDetailsWhere.title", routes.SkeletonJourneyController.goodsDetailsCost()))
  }

  val goodsDetailsCost: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"goodsDetailsCost.title", routes.SkeletonJourneyController.goodsReview()))
  }

  val goodsReview: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"goodsReview.title", routes.SkeletonJourneyController.calculation()))
  }

  val calculation: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"calculation.title", routes.SkeletonJourneyController.traderAgent()))
  }

  val traderAgent: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"traderAgent.title", routes.SkeletonJourneyController.traderDetails()))
  }

  val traderDetails: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"traderDetails.title", routes.SkeletonJourneyController.traderAddress()))
  }

  val traderAddress: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"traderAddress.title", routes.SkeletonJourneyController.traderAddressList()))
  }

  val traderAddressList: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"traderAddressList.title", routes.SkeletonJourneyController.traderEori()))
  }

  val traderEori: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"traderEori.title", routes.SkeletonJourneyController.traderJourney()))
  }

  val traderJourney: Action[AnyContent] = Action { implicit request =>
    Ok(page(s"traderJourney.title", routes.CheckYourAnswersController.onPageLoad()))
  }
}

/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
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

  val start: Action[AnyContent] =
    displaySkeletonPage("start.title", routes.SkeletonJourneyController.importExport())

  val importExport: Action[AnyContent] =
    displaySkeletonPage("importExport.title", routes.SkeletonJourneyController.multipleCountriesEuCheck())

  val multipleCountriesEuCheck: Action[AnyContent] =
    displaySkeletonPage("multipleCountriesEuCheck.title", routes.SkeletonJourneyController.goodsExcise())

  val goodsExcise: Action[AnyContent] =
    displaySkeletonPage("goodsExcise.title", routes.SkeletonJourneyController.valueWeightOfGoods())

  val valueWeightOfGoods: Action[AnyContent] =
    displaySkeletonPage("valueWeightOfGoods.title", routes.SkeletonJourneyController.goodsType())

  val goodsType: Action[AnyContent] =
    displaySkeletonPage("goodsType.title", routes.SkeletonJourneyController.goodsCategory())

  val goodsCategory: Action[AnyContent] =
    displaySkeletonPage("goodsCategory.title", routes.SkeletonJourneyController.goodsDetailsWhere())

  val goodsDetailsWhere: Action[AnyContent] =
    displaySkeletonPage("goodsDetailsWhere.title", routes.SkeletonJourneyController.goodsDetailsCost())

  val goodsDetailsCost: Action[AnyContent] =
    displaySkeletonPage("goodsDetailsCost.title", routes.SkeletonJourneyController.goodsReview())

  val goodsReview: Action[AnyContent] =
    displaySkeletonPage("goodsReview.title", routes.SkeletonJourneyController.calculation())

  val calculation: Action[AnyContent] =
    displaySkeletonPage("calculation.title", routes.SkeletonJourneyController.traderAgent())

  val traderAgent: Action[AnyContent] =
    displaySkeletonPage("traderAgent.title", routes.SkeletonJourneyController.traderDetails())

  val traderDetails: Action[AnyContent] =
    displaySkeletonPage("traderDetails.title", routes.SkeletonJourneyController.traderAddress())

  val traderAddress: Action[AnyContent] =
    displaySkeletonPage("traderAddress.title", routes.SkeletonJourneyController.traderAddressList())

  val traderAddressList: Action[AnyContent] =
    displaySkeletonPage("traderAddressList.title", routes.SkeletonJourneyController.traderEori())

  val traderEori: Action[AnyContent] =
    displaySkeletonPage("traderEori.title", routes.SkeletonJourneyController.traderJourney())

  val traderJourney: Action[AnyContent] =
    displaySkeletonPage("traderJourney.title", routes.CheckYourAnswersController.onPageLoad())

  private def displaySkeletonPage(titleMessageKey: String, next: Call) = Action { implicit request =>
    Ok(page(titleMessageKey, next))
  }
}
